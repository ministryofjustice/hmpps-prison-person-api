package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.DocumentServiceExtension.Companion.documentService

class PrisonerPhotographControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /photographs/{prisonerNumber}/all")
  @Nested
  inner class ViewPrisonPersonDataTest {

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
    inner class HappyPath {

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
        val documentResponse = listOf(
          DocumentDto(
            documentUuid = "abc",
            documentName = "name",
            documentFileName = "fileName",
            fileName = "fileName",
            fileExtension = "jpg",
            fileSize = 80,
            fileHash = "hash",
            mimeType = "mime",
            metadata = mapOf("prisonerNumber" to "A1234AA"),
            createdTime = "2021-01-01T00:00:00",
            createdByServiceName = "service",
            createdByUsername = "user",
          ),
        )
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
}
