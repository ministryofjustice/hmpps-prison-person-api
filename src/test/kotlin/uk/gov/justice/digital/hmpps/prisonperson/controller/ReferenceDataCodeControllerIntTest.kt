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
        webTestClient.get().uri("/reference-data/domains/FACE/codes")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/FACE/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data codes`() {
        webTestClient.get().uri("/reference-data/domains/FACE/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              [
                {
                  "domain": "FACE",
                  "code": "ANGULAR",
                  "description": "Angular",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "FACE",
                  "code": "BULLET",
                  "description": "Long",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "FACE",
                  "code": "OVAL",
                  "description": "Oval",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "FACE",
                  "code": "ROUND",
                  "description": "Round",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "FACE",
                  "code": "SQUARE",
                  "description": "Square",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
                  "createdBy": "OMS_OWNER"
                },
                {
                  "domain": "FACE",
                  "code": "TRIANGULAR",
                  "description": "Triangle",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
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
        webTestClient.get().uri("/reference-data/domains/FACE/codes/OVAL")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/FACE/codes/OVAL")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data code`() {
        webTestClient.get().uri("/reference-data/domains/FACE/codes/OVAL")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
                {
                  "domain": "FACE",
                  "code": "OVAL",
                  "description": "Oval",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
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
        webTestClient.get().uri("/reference-data/domains/FACE/codes/UNKNOWN")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
      }
    }
  }
}
