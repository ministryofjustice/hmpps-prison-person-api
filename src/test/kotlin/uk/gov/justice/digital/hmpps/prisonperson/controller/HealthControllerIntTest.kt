package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import java.time.Clock

class HealthControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("PATCH /prisoners/{prisonerNumber}/health")
  @Nested
  inner class SetHealthTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/health").header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange().expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/health").headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json").bodyValue(VALID_REQUEST_BODY).exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/health")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG"))).header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange().expectStatus().isForbidden
      }
    }

    @Nested
    @Sql("classpath:jpa/repository/reset.sql")
    inner class Validation {

      @Test
      fun `bad request when field type is not as expected`() {
        expectBadRequestFrom(
          requestBody = """{ "smokerOrVaper": 123 }""",
          message = "Validation failure: Couldn't read request body",
        )
      }

      @Test
      fun `bad request when prisoner not found`() {
        webTestClient.patch().uri("/prisoners/PRISONER_NOT_FOUND/health")
          .headers(setAuthorisation(USER1, roles = listOf("ROLE_PRISON_PERSON_API__HEALTH__RW")))
          .header("Content-Type", "application/json").bodyValue(VALID_REQUEST_BODY).exchange()
          .expectStatus().isBadRequest.expectBody().jsonPath("userMessage")
          .isEqualTo("Validation failure: Prisoner number 'PRISONER_NOT_FOUND' not found")
      }

      private fun expectBadRequestFrom(requestBody: String, message: String) {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/health")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__HEALTH__RW")))
          .header("Content-Type", "application/json").bodyValue(requestBody).exchange()
          .expectStatus().isBadRequest.expectBody().jsonPath("userMessage").isEqualTo(message)
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can create new health information`() {
        expectSuccessfulUpdateFrom(VALID_REQUEST_BODY).expectBody().json(
          SMOKER_NO_RESPONSE,
          true,
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/health.sql")
      fun `can update existing health information`() {
        expectSuccessfulUpdateFrom(VALID_REQUEST_BODY).expectBody().json(
          SMOKER_NO_RESPONSE,
          true,
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/health.sql")
      fun `can update existing health information to null`() {
        expectSuccessfulUpdateFrom(
          // language=json
          """
            { "smokerOrVaper": null }
          """.trimIndent(),
        ).expectBody().json(
          // language=json
          """
            { "smokerOrVaper": null }
          """.trimIndent(),
          true,
        )
      }

      @Test
      @Disabled("Skipped until history implemented")
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can update an existing set of health information a number of times`() {
      }

      @Test
      @Disabled("Skipped until domain events are implemented")
      @Sql("classpath:jpa/repository/reset.sql")
      fun `should publish domain event`() {
      }

      private fun expectSuccessfulUpdateFrom(requestBody: String, user: String? = USER1) =
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/health")
          .headers(setAuthorisation(user, roles = listOf("ROLE_PRISON_PERSON_API__HEALTH__RW")))
          .header("Content-Type", "application/json").bodyValue(requestBody).exchange().expectStatus().isOk
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    val VALID_REQUEST_BODY =
      // language=json
      """
        { 
          "smokerOrVaper": "SMOKE_NO"
        }
      """.trimIndent()

    val SMOKER_NO_RESPONSE =
      // language=json
      """
      {
        "smokerOrVaper": {
          "id": "SMOKE_NO",
          "domain": "SMOKE",
          "code": "NO",
          "description": "No, they do not smoke or vape",
          "listSequence": 0,
          "isActive": true,
          "createdAt": "2024-07-11T16:00:00Z",
          "createdBy": "OMS_OWNER"
        } 
      }
      """.trimIndent()
  }
}
