package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import java.time.Clock

class PhysicalAttributesControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("PUT /prisoners/{prisonerNumber}/physical-attributes")
  @Nested
  inner class ViewPrisonPersonDataTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/prisoners/A1234AA/physical-attributes")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(
            // language=json
            """
              { 
                "height": 180,
                "weight": 70
              }
            """.trimIndent(),
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .bodyValue(
            // language=json
            """
              { 
                "height": 180,
                "weight": 70
              }
            """.trimIndent(),
          )
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    @Sql("classpath:jpa/repository/reset.sql")
    inner class Validation {

      @Test
      fun `bad request when height below 54cm`() {
        expectBadRequestFrom(
          requestBody = """{ "height": 53 }""",
          message = "Validation failure(s): The height must be a plausible value in centimetres (between 54 and 272)",
        )
      }

      @Test
      fun `bad request when height exceeds 272cm`() {
        expectBadRequestFrom(
          requestBody = """{ "height": 273 }""",
          message = "Validation failure(s): The height must be a plausible value in centimetres (between 54 and 272)",
        )
      }

      @Test
      fun `bad request when weight below 2kg`() {
        expectBadRequestFrom(
          requestBody = """{ "weight": 1 }""",
          message = "Validation failure(s): The weight must be a plausible value in kilograms (between 2 and 635)",
        )
      }

      @Test
      fun `bad request when weight exceeds 635kg`() {
        expectBadRequestFrom(
          requestBody = """{ "weight": 636 }""",
          message = "Validation failure(s): The weight must be a plausible value in kilograms (between 2 and 635)",
        )
      }

      private fun expectBadRequestFrom(requestBody: String, message: String) {
        webTestClient.put().uri("/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody().jsonPath("userMessage").isEqualTo(message)
      }
    }

    @Nested
    @Sql("classpath:jpa/repository/reset.sql")
    inner class HappyPath {

      @Test
      fun `currently not implemented`() {
        webTestClient.put().uri("/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(
            // language=json
            """
              { 
                "height": 180,
                "weight": 70
              }
            """.trimIndent(),
          )
          .exchange()
          .expectStatus().isEqualTo(HttpStatus.NOT_IMPLEMENTED)
      }
    }
  }
}
