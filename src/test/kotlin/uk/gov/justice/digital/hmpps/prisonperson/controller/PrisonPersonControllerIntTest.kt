package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.time.Clock

const val EXPECTED_USERNAME = "A_TEST_USER"

@WithMockUser(username = EXPECTED_USERNAME)
class PrisonPersonControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @Autowired
  lateinit var repository: PhysicalAttributesRepository

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
      "classpath:controller/physical_attributes.sql",
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
                  "height": 180,
                  "weight": 70
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
