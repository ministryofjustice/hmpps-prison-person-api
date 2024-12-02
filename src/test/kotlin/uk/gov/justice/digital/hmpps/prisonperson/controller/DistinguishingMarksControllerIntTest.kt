package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkImageDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.DocumentServiceExtension.Companion.documentService
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMark
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMarkImage
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.utils.UuidV7Generator.Companion.uuidGenerator
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import java.util.stream.Stream

class DistinguishingMarksControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @TestConfiguration
  @DisplayName("/distinguishingMarks")
  @Nested
  inner class DistinguishingMarksControllerIntTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/distinguishing-marks/prisoner/A1234AA")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/distinguishing-marks/prisoner/A1234AA")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/distinguishing-marks/prisoner/A1234AA")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class GetDistinguishingMarksForPrisonerHappyPath {

      @Test
      fun `can return empty list when none are found`() {
        webTestClient.get().uri("/distinguishing-marks/prisoner/A1234AA")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            // language=json
            """
              []
            """.trimIndent(),
            true,
          )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/distinguishing_marks/distinguishing_marks.sql")
      fun `can return list of distinguishing mark data for prisoner when found`() {
        val response = webTestClient.get().uri("/distinguishing-marks/prisoner/12345")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBodyList(DistinguishingMarkDto::class.java)
          .returnResult()
          .responseBody

        assertThat(response).hasSize(2)
        assertThat(response?.get(0)?.id).isEqualTo("c46d0ce9-e586-4fa6-ae76-52ea8c242257")
        assertThat(response?.get(1)?.id).isEqualTo("c46d0ce9-e586-4fa6-ae76-52ea8c242258")
      }
    }

    @Nested
    inner class GetDistinguishingMarkHappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can return 404 when not found`() {
        webTestClient.get().uri("/distinguishing-marks/mark/c46d0ce9-e586-4fa6-ae76-52ea8c242258")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/distinguishing_marks/distinguishing_marks.sql")
      fun `can return distinguishing mark data when found`() {
        val response = webTestClient.get().uri("/distinguishing-marks/mark/c46d0ce9-e586-4fa6-ae76-52ea8c242258")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody(DistinguishingMarkDto::class.java)
          .returnResult()
          .responseBody

        assertThat(response?.id).isEqualTo("c46d0ce9-e586-4fa6-ae76-52ea8c242258")
        assertThat(response?.prisonerNumber).isEqualTo("12345")
        assertThat(response?.bodyPart).isEqualTo(ReferenceDataSimpleDto("BODY_PART_FACE", "Face", 0, true))
        assertThat(response?.markType).isEqualTo(ReferenceDataSimpleDto("MARK_TYPE_SCAR", "Scar", 0, true))
        assertThat(response?.side).isEqualTo(ReferenceDataSimpleDto("SIDE_L", "Left", 2, true))
        assertThat(response?.partOrientation).isEqualTo(ReferenceDataSimpleDto("PART_ORIENT_CENTR", "Centre", 0, true))
        assertThat(response?.comment).isEqualTo("Another scar")
        assertThat(response?.photographUuids).containsExactlyInAnyOrder(
          DistinguishingMarkImageDto("c46d0ce9-e586-4fa6-ae76-52ea8c242260", false),
          DistinguishingMarkImageDto("c46d0ce9-e586-4fa6-ae76-52ea8c242261", true),
        )
        assertThat(response?.createdAt).isEqualTo(ZonedDateTime.parse("2024-01-02T09:10:11+00:00"))
        assertThat(response?.createdBy).isEqualTo("USER_GEN")
      }
    }

    @Nested
    inner class PostDistinguishingMarkHappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can post physical identifier picture to document service`() {
        mockkObject(uuidGenerator)
        val markUuid = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242257")
        val docUuid = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242258")
        every { uuidGenerator.generate() } returns docUuid andThen markUuid

        documentService.stubPostNewDocument(
          uuid = docUuid.toString(),
          documentType = DocumentType.DISTINGUISHING_MARK_IMAGE,
          result = DOCUMENT_DTO,
        )

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ByteArrayResource(MULTIPART_FILE.bytes))
          .header("Content-Disposition", "form-data; name=file; filename=filename.jpg")
        bodyBuilder.part("prisonerNumber", "A1234AA")
        bodyBuilder.part("bodyPart", "BODY_PART_LEG")
        bodyBuilder.part("markType", "MARK_TYPE_TAT")
        bodyBuilder.part("side", "SIDE_R")
        bodyBuilder.part("partOrientation", "PART_ORIENT_CENTR")
        bodyBuilder.part("comment", "Comment")

        val response = webTestClient.post().uri("/distinguishing-marks/mark")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
          .exchange()
          .expectStatus().isOk
          .expectBody(DistinguishingMarkDto::class.java)
          .returnResult()
          .responseBody

        assertThat(response?.id).isEqualTo(markUuid.toString())
        assertThat(response?.prisonerNumber).isEqualTo("A1234AA")
        assertThat(response?.bodyPart).isEqualTo(ReferenceDataSimpleDto("BODY_PART_LEG", "Leg", 0, true))
        assertThat(response?.markType).isEqualTo(ReferenceDataSimpleDto("MARK_TYPE_TAT", "Tattoo", 0, true))
        assertThat(response?.side).isEqualTo(ReferenceDataSimpleDto("SIDE_R", "Right", 1, true))
        assertThat(response?.partOrientation).isEqualTo(ReferenceDataSimpleDto("PART_ORIENT_CENTR", "Centre", 0, true))
        assertThat(response?.comment).isEqualTo("Comment")
        assertThat(response?.photographUuids).containsExactly(
          DistinguishingMarkImageDto(
            DOCUMENT_DTO.documentUuid,
            true,
          ),
        )
        assertThat(response?.createdAt is ZonedDateTime)
        assertThat(response?.createdBy).isEqualTo("prison-person-api-client")

        unmockkObject(uuidGenerator)

        expectDistinguishingMarkHistory(
          markUuid,
          HistoryComparison(
            value = DistinguishingMark(
              distinguishingMarkId = markUuid,
              prisonerNumber = "A1234AA",
              bodyPart = BODY_PART_LEG,
              markType = MARK_TYPE_TATTOO,
              side = SIDE_RIGHT,
              partOrientation = ORIENTATION_CENTRE,
              comment = "Comment",
              createdAt = NOW,
              createdBy = "prison-person-api-client",
            ).apply {
              photographUuids =
                mutableListOf(DistinguishingMarkImage(UUID.fromString(DOCUMENT_DTO.documentUuid), this, true))
            },
            createdBy = "prison-person-api-client",
            createdAt = NOW_WITH_ZONE,
            appliesFrom = NOW,
            appliesTo = null,
            source = Source.DPS,
            anomalous = false,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can post physical identifier without image`() {
        mockkObject(uuidGenerator)
        val markUuid = UUID.randomUUID()
        every { uuidGenerator.generate() } returns markUuid

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("prisonerNumber", "A1234AA")
        bodyBuilder.part("bodyPart", "BODY_PART_LEG")
        bodyBuilder.part("markType", "MARK_TYPE_TAT")
        bodyBuilder.part("side", "SIDE_R")
        bodyBuilder.part("partOrientation", "PART_ORIENT_CENTR")
        bodyBuilder.part("comment", "Comment")

        val response = webTestClient.post().uri("/distinguishing-marks/mark")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
          .exchange()
          .expectStatus().isOk
          .expectBody(DistinguishingMarkDto::class.java)
          .returnResult()
          .responseBody

        assertThat(response?.id).isEqualTo(markUuid.toString())
        assertThat(response?.prisonerNumber).isEqualTo("A1234AA")
        assertThat(response?.bodyPart).isEqualTo(ReferenceDataSimpleDto("BODY_PART_LEG", "Leg", 0, true))
        assertThat(response?.markType).isEqualTo(ReferenceDataSimpleDto("MARK_TYPE_TAT", "Tattoo", 0, true))
        assertThat(response?.side).isEqualTo(ReferenceDataSimpleDto("SIDE_R", "Right", 1, true))
        assertThat(response?.partOrientation).isEqualTo(ReferenceDataSimpleDto("PART_ORIENT_CENTR", "Centre", 0, true))
        assertThat(response?.comment).isEqualTo("Comment")
        assertThat(response?.photographUuids).isEmpty()
        assertThat(response?.createdAt is ZonedDateTime)
        assertThat(response?.createdBy).isEqualTo("prison-person-api-client")

        unmockkObject(uuidGenerator)

        expectDistinguishingMarkHistory(
          markUuid,
          HistoryComparison(
            value = DistinguishingMark(
              distinguishingMarkId = markUuid,
              prisonerNumber = "A1234AA",
              bodyPart = BODY_PART_LEG,
              markType = MARK_TYPE_TATTOO,
              side = SIDE_RIGHT,
              partOrientation = ORIENTATION_CENTRE,
              comment = "Comment",
              createdAt = NOW,
              createdBy = "prison-person-api-client",
            ),
            createdBy = "prison-person-api-client",
            createdAt = NOW_WITH_ZONE,
            appliesFrom = NOW,
            appliesTo = null,
            source = Source.DPS,
            anomalous = false,
          ),
        )
      }
    }

    @Nested
    inner class PostMarkValidation {
      // may change with emerging criteria
      @ParameterizedTest
      @CsvSource(
        "'', 'BODY_PART_LEG', 'MARK_TYPE_TAT', 'SIDE_R', 'PART_ORIENT_CENTR', 'Comment'",
        "'A1234AA', '', 'MARK_TYPE_TAT', 'SIDE_R', 'PART_ORIENT_CENTR', 'Comment'",
        "'A1234AA', 'BODY_PART_LEG', '', 'SIDE_R', 'PART_ORIENT_CENTR', 'Comment'",
      )
      fun `bad request when missing required fields`(
        prisonerNumber: String,
        bodyPart: String,
        markType: String,
        side: String,
        partOrientation: String,
        comment: String,
      ) {
        val bodyBuilder = MultipartBodyBuilder()
        if (prisonerNumber.isNotEmpty()) bodyBuilder.part("prisonerNumber", prisonerNumber)
        if (bodyPart.isNotEmpty()) bodyBuilder.part("bodyPart", bodyPart)
        if (markType.isNotEmpty()) bodyBuilder.part("markType", markType)
        if (side.isNotEmpty()) bodyBuilder.part("side", side)
        if (partOrientation.isNotEmpty()) bodyBuilder.part("partOrientation", partOrientation)
        if (comment.isNotEmpty()) bodyBuilder.part("comment", comment)

        webTestClient.post().uri("/distinguishing-marks/mark")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
          .exchange()
          .expectStatus().isBadRequest
      }
    }

    // Update routes
    @Nested
    inner class UpdateMark {
      @Nested
      inner class PatchMarkValidation {
        @ParameterizedTest(name = "PATCH Mark invalid request: {0}")
        @MethodSource("uk.gov.justice.digital.hmpps.prisonperson.controller.DistinguishingMarksControllerIntTest#patchMarkValidations")
        fun `PATCH mark validations`(requestBody: String, message: String) {
          webTestClient.patch().uri("/distinguishing-marks/mark/$MARK_ONE_ID")
            .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
            .header("Content-Type", "application/json").bodyValue(requestBody).exchange()
            .expectStatus().isBadRequest.expectBody().jsonPath("userMessage").isEqualTo(message)
        }
      }

      @Nested
      inner class PatchMarkHappyPath {
        @Test
        @Sql("classpath:jpa/repository/reset.sql")
        @Sql("classpath:controller/distinguishing_marks/distinguishing_marks.sql")
        @Sql("classpath:controller/distinguishing_marks/distinguishing_mark_history.sql")
        fun `can update an existing distinguishing mark`() {
          webTestClient.patch().uri("/distinguishing-marks/mark/c46d0ce9-e586-4fa6-ae76-52ea8c242257")
            .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
            .header("Content-Type", "application/json")
            .bodyValue(
              // language=json
              """
              {
                "bodyPart": "BODY_PART_ARM",
                "markType": "MARK_TYPE_TAT",
                "side": "SIDE_L",
                "partOrientation": "PART_ORIENT_LOW",
                "comment": "It's a tattoo now"
              }
              """.trimIndent(),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json(
              // language=json
              """
              {
                "id": "c46d0ce9-e586-4fa6-ae76-52ea8c242257",
                "prisonerNumber": "12345",
                "bodyPart": {
                  "id": "BODY_PART_ARM",
                  "description": "Arm",
                  "listSequence": 0,
                  "isActive": true
                },
                "markType": {
                  "id": "MARK_TYPE_TAT",
                  "description": "Tattoo",
                  "listSequence": 0,
                  "isActive": true
                },
                "side": {
                  "id": "SIDE_L",
                  "description": "Left",
                  "listSequence": 2,
                  "isActive": true
                },
                "partOrientation": {
                  "id": "PART_ORIENT_LOW",
                  "description": "Low",
                  "listSequence": 0,
                  "isActive": true
                },
                "comment": "It's a tattoo now",
                "photographUuids": [],
                "createdAt": "2024-01-02T09:10:11+0000",
                "createdBy": "USER_GEN"
              }
              """.trimIndent(),
              true,
            )

          val markUuid = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242257")

          expectDistinguishingMarkHistory(
            markUuid,
            HistoryComparison(
              value = DistinguishingMark(
                distinguishingMarkId = markUuid,
                prisonerNumber = "12345",
                bodyPart = BODY_PART_FACE,
                markType = MARK_TYPE_SCAR,
                side = SIDE_RIGHT,
                partOrientation = ORIENTATION_CENTRE,
                comment = "Large scar from fight",
                createdAt = THEN,
                createdBy = "USER_GEN",
              ),
              createdBy = "USER1",
              createdAt = THEN,
              appliesFrom = THEN,
              appliesTo = NOW,
              source = Source.DPS,
              anomalous = false,
            ),

            HistoryComparison(
              value = DistinguishingMark(
                distinguishingMarkId = markUuid,
                prisonerNumber = "12345",
                bodyPart = BODY_PART_ARM,
                markType = MARK_TYPE_TATTOO,
                side = SIDE_LEFT,
                partOrientation = ORIENTATION_LOW,
                comment = "It's a tattoo now",
                createdAt = THEN,
                createdBy = "USER_GEN",
              ),
              createdBy = "prison-person-api-client",
              createdAt = NOW,
              appliesFrom = NOW,
              appliesTo = null,
              source = Source.DPS,
              anomalous = false,
            ),
          )
        }
      }

      @Nested
      inner class UpdateImageHappyPath {
        @Test
        @Sql("classpath:jpa/repository/reset.sql")
        @Sql("classpath:controller/distinguishing_marks/distinguishing_marks.sql")
        @Sql("classpath:controller/distinguishing_marks/distinguishing_mark_history.sql")
        fun `can update the image on a mark`() {
          mockkObject(uuidGenerator)
          val docUuid = UUID.randomUUID()
          every { uuidGenerator.generate() } returns docUuid

          documentService.stubPostNewDocument(
            uuid = docUuid.toString(),
            documentType = DocumentType.DISTINGUISHING_MARK_IMAGE,
            result = DOCUMENT_DTO,
          )

          val bodyBuilder = MultipartBodyBuilder()
          bodyBuilder.part("file", ByteArrayResource(MULTIPART_FILE.bytes))
            .header("Content-Disposition", "form-data; name=file; filename=filename.jpg")

          val response =
            webTestClient.post().uri("/distinguishing-marks/mark/c46d0ce9-e586-4fa6-ae76-52ea8c242258/photo")
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .bodyValue(bodyBuilder.build())
              .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
              .exchange()
              .expectStatus().isOk
              .expectBody(DistinguishingMarkDto::class.java)
              .returnResult()
              .responseBody

          assertThat(response?.photographUuids).containsExactlyInAnyOrder(
            DistinguishingMarkImageDto("c46d0ce9-e586-4fa6-ae76-52ea8c242260", false),
            DistinguishingMarkImageDto("c46d0ce9-e586-4fa6-ae76-52ea8c242261", false),
            DistinguishingMarkImageDto(DOCUMENT_DTO.documentUuid, true),
          )

          val markUuid = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242258")
          expectDistinguishingMarkHistory(
            markUuid,
            HistoryComparison(
              value = DistinguishingMark(
                distinguishingMarkId = markUuid,
                prisonerNumber = "12345",
                bodyPart = BODY_PART_FACE,
                markType = MARK_TYPE_SCAR,
                side = SIDE_RIGHT,
                partOrientation = ORIENTATION_CENTRE,
                comment = "Another scar",
                createdAt = THEN,
                createdBy = "USER_GEN",
              ).apply {
                photographUuids = mutableListOf(
                  DistinguishingMarkImage(UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242260"), this, false),
                  DistinguishingMarkImage(UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242261"), this, true),
                )
              },
              createdBy = "USER1",
              createdAt = THEN,
              appliesFrom = THEN,
              appliesTo = NOW,
              source = Source.DPS,
              anomalous = false,
            ),

            HistoryComparison(
              value = DistinguishingMark(
                distinguishingMarkId = markUuid,
                prisonerNumber = "12345",
                bodyPart = BODY_PART_FACE,
                markType = MARK_TYPE_SCAR,
                side = SIDE_LEFT,
                partOrientation = ORIENTATION_CENTRE,
                comment = "Another scar",
                createdAt = THEN,
                createdBy = "USER_GEN",
              ).apply {
                photographUuids = mutableListOf(
                  DistinguishingMarkImage(UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242260"), this, false),
                  DistinguishingMarkImage(UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242261"), this, false),
                  DistinguishingMarkImage(UUID.fromString(DOCUMENT_DTO.documentUuid), this, true),
                )
              },
              createdBy = "prison-person-api-client",
              createdAt = NOW,
              appliesFrom = NOW,
              appliesTo = null,
              source = Source.DPS,
              anomalous = false,
            ),
          )
        }
      }
    }
  }

  private companion object {
    val NOW = ZonedDateTime.now(clock)
    val NOW_WITH_ZONE = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val MARK_ONE_ID = "c46d0ce9-e586-4fa6-ae76-52ea8c242257"

    val DOCUMENT_DTO = DocumentDto(
      documentUuid = "c46d0ce9-e586-4fa6-ae76-52ea8c242263",
      documentType = DocumentType.DISTINGUISHING_MARK_IMAGE,
      documentFilename = "fileName",
      filename = "fileName",
      fileExtension = "jpg",
      fileSize = 80,
      fileHash = "hash",
      mimeType = "mime",
      metadata = mapOf("prisonerNumber" to "A1234AA"),
      createdTime = "2021-01-01T00:00:00",
      createdByServiceName = "service",
      createdByUsername = "user",
    )

    val MULTIPART_FILE: MultipartFile = MockMultipartFile(
      "file",
      "filename.jpg",
      MediaType.IMAGE_JPEG_VALUE,
      "mock content".toByteArray(),
    )

    val MARK_TYPE_VALIDATION_ERROR =
      "Validation failure(s): Type of distinguishing mark should a reference data code ID in the correct domain, or Undefined."
    val BODY_PART_VALIDATION_ERROR =
      "Validation failure(s): Body part of distinguishing mark should a reference data code ID in the correct domain, or Undefined."
    val PATCH_MARK_VALIDATION_ERROR =
      "Validation failure(s): The value must be a reference domain code id of the correct domain, null, or Undefined."

    @JvmStatic
    fun patchMarkValidations(): Stream<Arguments> {
      return Stream.of(
        Arguments.of("""{"markType": "SIDE_R"}""".trimMargin(), MARK_TYPE_VALIDATION_ERROR),
        Arguments.of("""{"markType": null}""".trimMargin(), MARK_TYPE_VALIDATION_ERROR),
        Arguments.of("""{"bodyPart": "SIDE_R"}""".trimMargin(), BODY_PART_VALIDATION_ERROR),
        Arguments.of("""{"bodyPart": null}""".trimMargin(), BODY_PART_VALIDATION_ERROR),
        Arguments.of("""{"side": "MARK_TYPE_TAT"}""".trimMargin(), PATCH_MARK_VALIDATION_ERROR),
        Arguments.of("""{"partOrientation": "MARK_TYPE_TAT"}""".trimMargin(), PATCH_MARK_VALIDATION_ERROR),
      )
    }

    val BODY_PART_FACE = ReferenceDataCode(
      id = "BODY_PART_FACE",
      description = "Face",
      listSequence = 0,
      code = "FACE",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "BODY_PART",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Body part distinguishing mark is on",
      ),
    )

    val MARK_TYPE_SCAR = ReferenceDataCode(
      id = "MARK_TYPE_SCAR",
      description = "Scar",
      listSequence = 0,
      code = "SCAR",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "MARK_TYPE",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Type of distinguishing mark",
      ),
    )

    val SIDE_RIGHT = ReferenceDataCode(
      id = "SIDE_R",
      description = "Right",
      listSequence = 1,
      code = "R",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "SIDE",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Side distinguishing mark is on",
      ),
    )

    val ORIENTATION_CENTRE = ReferenceDataCode(
      id = "PART_ORIENT_CENTR",
      description = "Centre",
      listSequence = 0,
      code = "CENTR",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "PART_ORIENT",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Position of distinguishing mark",
      ),
    )

    val BODY_PART_ARM = ReferenceDataCode(
      id = "BODY_PART_ARM",
      description = "Arm",
      listSequence = 0,
      code = "ARM",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "BODY_PART",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Body part distinguishing mark is on",
      ),
    )

    val MARK_TYPE_TATTOO = ReferenceDataCode(
      id = "MARK_TYPE_TAT",
      description = "Tattoo",
      listSequence = 0,
      code = "TAT",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "MARK_TYPE",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Type of distinguishing mark",
      ),
    )

    val SIDE_LEFT = ReferenceDataCode(
      id = "SIDE_L",
      description = "Left",
      listSequence = 2,
      code = "",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "SIDE",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Side distinguishing mark is on",
      ),
    )

    val ORIENTATION_LOW = ReferenceDataCode(
      id = "PART_ORIENT_LOW",
      description = "Low",
      listSequence = 0,
      code = "LOW",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "PART_ORIENT",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "Position of distinguishing mark",
      ),
    )

    val BODY_PART_LEG = ReferenceDataCode(
      id = "BODY_PART_LEG",
      description = "Leg",
      listSequence = 0,
      code = "PART_LEG",
      createdBy = "",
      createdAt = NOW,
      domain = ReferenceDataDomain(
        code = "",
        listSequence = 0,
        createdBy = "",
        createdAt = NOW,
        description = "",
      ),
    )
  }
}
