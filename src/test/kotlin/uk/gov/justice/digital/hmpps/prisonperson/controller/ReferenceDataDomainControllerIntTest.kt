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
          .expectBody()
          .jsonPath("$.length()").isEqualTo(12)
          .jsonPath("$[?(@.code == 'FACE')].description").isEqualTo("Face shape")
          .jsonPath("$[?(@.code == 'FACE')].listSequence").isEqualTo(0)
          .jsonPath("$[?(@.code == 'FACE')].isActive").isEqualTo(true)
          .jsonPath("$[?(@.code == 'FACE')].createdAt").isEqualTo("2024-07-11T17:00:00+0100")
          .jsonPath("$[?(@.code == 'FACE')].createdBy").isEqualTo("OMS_OWNER")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes.length()").isEqualTo(7)
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[0].id").isEqualTo("FACE_ANGULAR")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[1].id").isEqualTo("FACE_BULLET")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[2].id").isEqualTo("FACE_OVAL")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[3].id").isEqualTo("FACE_ROUND")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[4].id").isEqualTo("FACE_SQUARE")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[5].id").isEqualTo("FACE_TRIANGULAR")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[6].id").isEqualTo("FACE_INACTIVE")
          .jsonPath("$[?(@.code == 'FACE')].referenceDataCodes[6].isActive").isEqualTo(false)
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].subDomains.length()").isEqualTo(1)
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].subDomains[0].code").isEqualTo("FREE_FROM")
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].subDomains[0].referenceDataCodes.length()").isEqualTo(2)
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].subDomains[0].referenceDataCodes[0].id").isEqualTo("FREE_FROM_MONOAMINE_OXIDASE_INHIBITORS")
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].subDomains[0].referenceDataCodes[1].id").isEqualTo("FREE_FROM_CHEESE")
      }
    }

    @Test
    fun `can retrieve reference data domains including sub-domains at the top level`() {
      webTestClient.get().uri("/reference-data/domains?includeSubDomains=true")
        .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(13)
        .jsonPath("$[?(@.code == 'FREE_FROM')].isActive").isEqualTo(true)
    }
  }

  @DisplayName("GET /reference-data/domains/{domain}")
  @Nested
  inner class GetReferenceDataDomainTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/FACE")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/FACE")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data domain`() {
        webTestClient.get().uri("/reference-data/domains/FACE")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              {
                "code": "FACE",
                "description": "Face shape",
                "listSequence": 0,
                "isActive": true,
                "createdAt": "2024-07-11T17:00:00+0100",
                "createdBy": "OMS_OWNER",
                "referenceDataCodes": [
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
                  },
                  {
                    "domain": "FACE",
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
