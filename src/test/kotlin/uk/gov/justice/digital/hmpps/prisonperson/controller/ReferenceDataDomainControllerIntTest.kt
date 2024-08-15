package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase

class ReferenceDataDomainControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /reference-data/domains")
  @Nested
  inner class GetReferenceDataDomainsTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data domains`() {
        webTestClient.get().uri("/reference-data/domains")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              [
                {
                  "code": "TEST",
                  "description": "Test domain",
                  "listSequence": 1,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
                  "createdBy": "OMS_OWNER",
                  "referenceDataCodes": [
                    {
                      "domain": "TEST",
                      "code": "ORANGE",
                      "description": "Orange",
                      "listSequence": 1,
                      "isActive": true,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "domain": "TEST",
                      "code": "BROWN",
                      "description": "Brown",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "domain": "TEST",
                      "code": "RED",
                      "description": "Red",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "domain": "TEST",
                      "code": "WHITE",
                      "description": "White",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "domain": "TEST",
                      "code": "INACTIVE",
                      "description": "Inactive code for tests",
                      "listSequence": 0,
                      "isActive": false,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER",
                      "lastModifiedAt": "2024-07-11T17:00:00+0100",
                      "lastModifiedBy": "OMS_OWNER",
                      "deactivatedAt": "2024-07-11T17:00:00+0100",
                      "deactivatedBy": "OMS_OWNER"
                    }
                  ]
                },
                {
                  "code": "HAIR",
                  "description": "Hair type or colour",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T17:00:00+0100",
                  "createdBy": "OMS_OWNER",
                  "referenceDataCodes": [
                    {
                      "id": "HAIR_BLACK",
                      "domain": "HAIR",
                      "code": "BLACK",
                      "description": "Black",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "id": "HAIR_GREY",
                      "domain": "HAIR",
                      "code": "GREY",
                      "description": "Grey",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "id": "HAIR_BLONDE",
                      "domain": "HAIR",
                      "code": "BLONDE",
                      "description": "Blonde",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T17:00:00+0100",
                      "createdBy": "OMS_OWNER"
                    }
                  ]
                },
                {
                  "code": "SMOKE",
                  "description": "Smoker or vaper",
                  "listSequence": 0,
                  "isActive": true,
                  "createdAt": "2024-07-11T16:00:00Z",
                  "createdBy": "OMS_OWNER",
                  "referenceDataCodes": [
                    {
                      "id": "SMOKE_SMOKER",
                      "domain": "SMOKE",
                      "code": "SMOKER",
                      "description": "Yes, they smoke",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T16:00:00Z",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "id": "SMOKE_VAPER",
                      "domain": "SMOKE",
                      "code": "VAPER",
                      "description": "Yes, they smoke or use nicotine replacement therapy (NRT)",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T16:00:00Z",
                      "createdBy": "OMS_OWNER"
                    },
                    {
                      "id": "SMOKE_NO",
                      "domain": "SMOKE",
                      "code": "NO",
                      "description": "No, they do not smoke or vape",
                      "listSequence": 0,
                      "isActive": true,
                      "createdAt": "2024-07-11T16:00:00Z",
                      "createdBy": "OMS_OWNER"
                    }
                  ]
                }
              ]

            """.trimIndent(),
          )
      }
    }
  }

  @DisplayName("GET /reference-data/domains/{domain}")
  @Nested
  inner class GetReferenceDataDomainTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/TEST")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/TEST")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data domain`() {
        webTestClient.get().uri("/reference-data/domains/TEST")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              {
                "code": "TEST",
                "description": "Test domain",
                "listSequence": 1,
                "isActive": true,
                "createdAt": "2024-07-11T17:00:00+0100",
                "createdBy": "OMS_OWNER",
                "referenceDataCodes": [
                  {
                    "domain": "TEST",
                    "code": "ORANGE",
                    "description": "Orange",
                    "listSequence": 1,
                    "isActive": true,
                    "createdAt": "2024-07-11T17:00:00+0100",
                    "createdBy": "OMS_OWNER"
                  },
                  {
                    "domain": "TEST",
                    "code": "BROWN",
                    "description": "Brown",
                    "listSequence": 0,
                    "isActive": true,
                    "createdAt": "2024-07-11T17:00:00+0100",
                    "createdBy": "OMS_OWNER"
                  },
                  {
                    "domain": "TEST",
                    "code": "RED",
                    "description": "Red",
                    "listSequence": 0,
                    "isActive": true,
                    "createdAt": "2024-07-11T17:00:00+0100",
                    "createdBy": "OMS_OWNER"
                  },
                  {
                    "domain": "TEST",
                    "code": "WHITE",
                    "description": "White",
                    "listSequence": 0,
                    "isActive": true,
                    "createdAt": "2024-07-11T17:00:00+0100",
                    "createdBy": "OMS_OWNER"
                  },
                  {
                    "domain": "TEST",
                    "code": "INACTIVE",
                    "description": "Inactive code for tests",
                    "listSequence": 0,
                    "isActive": false,
                    "createdAt": "2024-07-11T17:00:00+0100",
                    "createdBy": "OMS_OWNER",
                    "lastModifiedAt": "2024-07-11T17:00:00+0100",
                    "lastModifiedBy": "OMS_OWNER",
                    "deactivatedAt": "2024-07-11T17:00:00+0100",
                    "deactivatedBy": "OMS_OWNER"
                  }
                ]
              }
            """.trimIndent(),
          )
      }
    }

    @Nested
    inner class NotFound {

      @Test
      fun `receive a 404 when no reference data domain found`() {
        webTestClient.get().uri("/reference-data/domains/UNKNOWN")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
      }
    }
  }
}
