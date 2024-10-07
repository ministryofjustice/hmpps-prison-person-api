package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase

class PrisonPersonControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /prisoners/{prisonerNumber}")
  @Nested
  inner class ViewPrisonPersonDataTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/prisoners/A1234AA")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/prisoners/A1234AA")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/prisoners/A1234AA")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    @Sql(
      "classpath:jpa/repository/reset.sql",
      "classpath:controller/physicalattributes/physical_attributes.sql",
      "classpath:controller/physicalattributes/field_history.sql",
      "classpath:controller/physicalattributes/field_metadata.sql",
    )
    inner class HappyPath {

      @Test
      fun `can retrieve prison person data`() {
        webTestClient.get().uri("/prisoners/A1234AA")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            // language=json
            """
              {
                "prisonerNumber": "A1234AA",
                "physicalAttributes": {
                  "height": {
                    "value":180,
                    "lastModifiedAt":"2024-01-02T09:10:11+0000",
                    "lastModifiedBy":"USER1"
                  },
                  "weight": {
                    "value":70,
                    "lastModifiedAt":"2024-01-02T09:10:11+0000",
                    "lastModifiedBy":"USER1"
                  },
                  "hair": null,
                  "facialHair": null,
                  "face": null,
                  "build": null,
                  "leftEyeColour": null,
                  "rightEyeColour": null,
                  "shoeSize": null
                },
                "health": {
                  "smokerOrVaper": null,
                  "foodAllergies": [],
                  "medicalDietaryRequirements": []
                }
              }
            """.trimIndent(),
            true,
          )
      }
    }

    @Nested
    @Sql("classpath:jpa/repository/reset.sql")
    inner class NotFound {

      @Test
      fun `receive a 404 when no physical attributes for prisoner number`() {
        webTestClient.get().uri("/prisoners/A1234AA")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().json(
            // language=json
            """
              {
                "status": 404,
                "userMessage": "Prison person data not found: No data for 'A1234AA'",  
                "developerMessage": "No data for 'A1234AA'"  
              }
            """.trimIndent(),
          )
      }
    }
  }
}
