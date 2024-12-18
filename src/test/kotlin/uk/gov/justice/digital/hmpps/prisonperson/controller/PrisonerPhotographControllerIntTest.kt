package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.DocumentServiceExtension.Companion.documentService
import uk.gov.justice.digital.hmpps.prisonperson.utils.UuidV7Generator.Companion.uuidGenerator
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

class PrisonerPhotographControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /photographs/{prisonerNumber}/all")
  @Nested
  inner class PrisonerPhotographControllerIntTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/photographs/A1234AA/all")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/photographs/A1234AA/all")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/photographs/A1234AA/all")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class GetAllHappyPath {

      @Test
      fun `can return photograph data for prisoner when none are found`() {
        documentService.stubGetAllPicturesForPrisoner()

        webTestClient.get().uri("/photographs/A1234AA/all")
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
      fun `can return photograph data for prisoner when documents are found`() {
        val documentResponse = listOf(DOCUMENT_DTO)

        documentService.stubGetAllPicturesForPrisoner(documentResponse)

        webTestClient.get().uri("/photographs/A1234AA/all")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(objectMapper.writeValueAsString(documentResponse))
      }
    }

    @Nested
    inner class PostImageHappyPath {

      @Test
      fun `can post prisoner profile picture to document service`() {
        mockkObject(uuidGenerator)
        val mockUuid = UUID.randomUUID()
        every { uuidGenerator.generate() } returns mockUuid

        documentService.stubPostNewDocument(uuid = mockUuid.toString(), result = DOCUMENT_DTO)

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ByteArrayResource(MULTIPART_FILE.bytes))
          .header("Content-Disposition", "form-data; name=file; filename=filename.jpg")
        bodyBuilder.part("prisonerNumber", "A1234AA")

        webTestClient.post().uri("/photographs/prisoner-profile")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(objectMapper.writeValueAsString(DOCUMENT_DTO))

        unmockkObject(uuidGenerator)
      }
    }

    @Nested
    inner class PostImageValidation {
      @Test
      fun `bad request when no file`() {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("prisonerNumber", "A1234AA")

        webTestClient.post().uri("/photographs/prisoner-profile")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isBadRequest
      }

      @Test
      fun `bad request when no prisonerNumber`() {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ByteArrayResource(MULTIPART_FILE.bytes))
          .header("Content-Disposition", "form-data; name=file; filename=filename.jpg")

        webTestClient.post().uri("/photographs/prisoner-profile")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isBadRequest
      }
    }

    @Nested
    inner class GetDocumentFileByUUI {
      @Test
      fun `can return document ByteArray with correct Content-Type header`() {
        val uuid = UUID.randomUUID().toString()
        documentService.stubGetDocumentByUuid(uuid)

        val expectedBytes = Files.readAllBytes(Paths.get("src/test/kotlin/uk/gov/justice/digital/hmpps/prisonperson/integration/assets/profile.jpeg"))

        webTestClient.get().uri("/photographs/$uuid/file")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectHeader().contentType(MediaType.IMAGE_JPEG)
          .expectBody()
          .returnResult()
          .responseBody?.let { actualBytes ->
            assertThat(actualBytes).isEqualTo(expectedBytes)
          }
      }
    }
  }

  private companion object {
    val DOCUMENT_DTO = DocumentDto(
      documentUuid = "abc",
      documentType = DocumentType.PRISONER_PROFILE_PICTURE,
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
