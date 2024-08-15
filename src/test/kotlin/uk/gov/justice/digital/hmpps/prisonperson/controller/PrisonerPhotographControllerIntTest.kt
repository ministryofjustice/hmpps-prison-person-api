package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
          .headers { headers ->
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/photographs/A1234AA/all")
          .headers { headers ->
            setAuthorisation(roles = listOf())(headers)
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/photographs/A1234AA/all")
          .headers { headers ->
            setAuthorisation(roles = listOf("ROLE_IS_WRONG"))(headers)
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
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
          .headers { headers ->
            setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO"))(headers)
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
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
          .headers { headers ->
            setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO"))(headers)
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
          .exchange()
          .expectStatus().isOk
          .expectBody().json(objectMapper.writeValueAsString(documentResponse))
      }

      @Nested
      inner class Validation {

        @Test
        fun `bad request when no Service-Name header`() {
          webTestClient.get().uri("/photographs/A1234AA/all")
            .headers { headers ->
              setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO"))(headers)
              headers.set("Username", "TEST_USER")
              headers.set("Active-Case-Load-Id", "MDI")
            }
            .exchange()
            .expectStatus().isBadRequest
        }
      }
    }

    @Nested
    inner class PostImageHappyPath {

      @Test
      fun `can post prisoner profile picture to document service`() {
        mockkStatic(UUID::class)
        val mockUuid = UUID.randomUUID().toString()
        every { UUID.randomUUID().toString() } returns mockUuid

        documentService.stubPostNewDocument(uuid = mockUuid, result = DOCUMENT_DTO)

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ByteArrayResource(MULTIPART_FILE.bytes))
          .header("Content-Disposition", "form-data; name=file; filename=filename.jpg")
        bodyBuilder.part("prisonerNumber", "A1234AA")

        webTestClient.post().uri("/photographs/prisoner-profile")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers { headers ->
            setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO"))(headers)
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
          .exchange()
          .expectStatus().isOk
          .expectBody().json(objectMapper.writeValueAsString(DOCUMENT_DTO))

        unmockkStatic(UUID::class)
      }
    }

    @Nested
    inner class PostImageValidation {

      @Test
      fun `bad request when no Service-Name header`() {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ByteArrayResource(MULTIPART_FILE.bytes))
          .header("Content-Disposition", "form-data; name=file; filename=filename.jpg")
        bodyBuilder.part("prisonerNumber", "A1234AA")

        webTestClient.post().uri("/photographs/prisoner-profile")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers { headers ->
            setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO"))(headers)
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
          .exchange()
          .expectStatus().isBadRequest
      }

      @Test
      fun `bad request when no file`() {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("prisonerNumber", "A1234AA")

        webTestClient.post().uri("/photographs/prisoner-profile")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(bodyBuilder.build())
          .headers { headers ->
            setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO"))(headers)
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
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
          .headers { headers ->
            setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO"))(headers)
            headers.set("Service-Name", "hmpps-prisoner-profile")
            headers.set("Username", "TEST_USER")
            headers.set("Active-Case-Load-Id", "MDI")
          }
          .exchange()
          .expectStatus().isBadRequest
      }
    }
  }

  companion object {
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
