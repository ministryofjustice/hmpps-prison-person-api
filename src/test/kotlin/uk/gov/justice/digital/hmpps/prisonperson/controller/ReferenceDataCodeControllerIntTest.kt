package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase

class ReferenceDataCodeControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /reference-data/domains/{domain}/codes")
  @Nested
  inner class GetReferenceDataCodesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/TEST/codes")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/TEST/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data codes`() {
        webTestClient.get().uri("/reference-data/domains/TEST/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              [
                {
                  "domain": "TEST",
                  "code": "ORANGE",
                  "description": "Orange",
                  "listSequence": 1,
                  "isActive": true,
                  "createdAt": "2024-07-11T16:00:00Z",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "TEST",
                  "code": "BROWN",
                  "description": "Brown",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T16:00:00Z",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "TEST",
                  "code": "RED",
                  "description": "Red",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T16:00:00Z",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "TEST",
                  "code": "WHITE",
                  "description": "White",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T16:00:00Z",
                  "createdBy": "OMS_OWNER"
                }
              ]
            """.trimIndent(),
          )
      }
    }
  }

  @DisplayName("GET /reference-data/domains/{domain}/codes/{code}")
  @Nested
  inner class GetReferenceDataCodeTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/TEST/codes/ORANGE")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/TEST/codes/ORANGE")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data code`() {
        webTestClient.get().uri("/reference-data/domains/TEST/codes/ORANGE")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
                {
                  "domain": "TEST",
                  "code": "ORANGE",
                  "description": "Orange",
                  "listSequence": 1,
                  "isActive": true,
                  "createdAt": "2024-07-11T16:00:00Z",
                  "createdBy": "OMS_OWNER"
                }
            """.trimIndent(),
          )
      }
    }

    @Nested
    inner class NotFound {

      @Test
      fun `receive a 404 when no reference data code found`() {
        webTestClient.get().uri("/reference-data/domains/TEST/codes/UNKNOWN")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
      }
    }
  }
}
