package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.IdentifyingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.DocumentServiceExtension.Companion.documentService
import uk.gov.justice.digital.hmpps.prisonperson.utils.UuidV7Generator.Companion.uuidGenerator
import java.time.ZonedDateTime
import java.util.UUID

class IdentifyingMarksControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  @DisplayName("/identifyingMarks")
  @Nested
  inner class IdentifyingMarksControllerIntTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/identifying-marks/prisoner/A1234AA")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/identifying-marks/prisoner/A1234AA")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/identifying-marks/prisoner/A1234AA")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class GetIdentifyingMarksForPrisonerHappyPath {

      @Test
      fun `can return empty list when none are found`() {
        webTestClient.get().uri("/identifying-marks/prisoner/A1234AA")
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
      @Sql("classpath:controller/identifying_marks/identifying_marks.sql")
      fun `can return list of identifying mark data for prisoner when found`() {
        val response = webTestClient.get().uri("/identifying-marks/prisoner/12345")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBodyList(IdentifyingMarkDto::class.java)
          .returnResult()
          .responseBody

        assertThat(response).hasSize(2)
        assertThat(response?.get(0)?.id).isEqualTo("c46d0ce9-e586-4fa6-ae76-52ea8c242257")
        assertThat(response?.get(1)?.id).isEqualTo("c46d0ce9-e586-4fa6-ae76-52ea8c242258")
      }
    }

    @Nested
    inner class GetIdentifyingMarkHappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can return 404 when not found`() {
        webTestClient.get().uri("/identifying-marks/mark/c46d0ce9-e586-4fa6-ae76-52ea8c242258")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/identifying_marks/identifying_marks.sql")
      fun `can return identifying mark data when found`() {
        val response = webTestClient.get().uri("/identifying-marks/mark/c46d0ce9-e586-4fa6-ae76-52ea8c242258")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody(IdentifyingMarkDto::class.java)
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
          "c46d0ce9-e586-4fa6-ae76-52ea8c242260",
          "c46d0ce9-e586-4fa6-ae76-52ea8c242261",
        )
        assertThat(response?.createdAt).isEqualTo(ZonedDateTime.parse("2024-01-02T09:10:11+00:00"))
        assertThat(response?.createdBy).isEqualTo("USER_GEN")
      }
    }

    @Nested
    inner class PostIdentifyingMarkHappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can post physical identifier picture to document service`() {
        mockkObject(uuidGenerator)
        val markUuid = UUID.randomUUID()
        val docUuid = UUID.randomUUID()
        every { uuidGenerator.generate() } returns docUuid andThen markUuid

        documentService.stubPostNewDocument(
          uuid = docUuid.toString(),
          documentType = DocumentType.PHYSICAL_IDENTIFIER_PICTURE,
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

        val response = webTestClient.post().uri("/identifying-marks/mark")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
          .exchange()
          .expectStatus().isOk
          .expectBody(IdentifyingMarkDto::class.java)
          .returnResult()
          .responseBody

        assertThat(response?.id).isEqualTo(markUuid.toString())
        assertThat(response?.prisonerNumber).isEqualTo("A1234AA")
        assertThat(response?.bodyPart).isEqualTo(ReferenceDataSimpleDto("BODY_PART_LEG", "Leg", 0, true))
        assertThat(response?.markType).isEqualTo(ReferenceDataSimpleDto("MARK_TYPE_TAT", "Tattoo", 0, true))
        assertThat(response?.side).isEqualTo(ReferenceDataSimpleDto("SIDE_R", "Right", 1, true))
        assertThat(response?.partOrientation).isEqualTo(ReferenceDataSimpleDto("PART_ORIENT_CENTR", "Centre", 0, true))
        assertThat(response?.comment).isEqualTo("Comment")
        assertThat(response?.photographUuids).containsExactly(DOCUMENT_DTO.documentUuid)
        assertThat(response?.createdAt is ZonedDateTime)
        assertThat(response?.createdBy).isEqualTo("prison-person-api-client")

        unmockkObject(uuidGenerator)
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

        val response = webTestClient.post().uri("/identifying-marks/mark")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
          .exchange()
          .expectStatus().isOk
          .expectBody(IdentifyingMarkDto::class.java)
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

        webTestClient.post().uri("/identifying-marks/mark")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW")))
          .exchange()
          .expectStatus().isBadRequest
      }
    }

    // PATCH routes
    @Nested
    inner class PatchMarkHappyPath {
      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/identifying_marks/identifying_marks.sql")
      fun `can update an existing identifying mark`() {
        webTestClient.patch().uri("/identifying-marks/mark/c46d0ce9-e586-4fa6-ae76-52ea8c242257")
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
      }
    }
  }

  private companion object {
    val DOCUMENT_DTO = DocumentDto(
      documentUuid = "c46d0ce9-e586-4fa6-ae76-52ea8c242260",
      documentType = DocumentType.PHYSICAL_IDENTIFIER_PICTURE,
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
  }
}
