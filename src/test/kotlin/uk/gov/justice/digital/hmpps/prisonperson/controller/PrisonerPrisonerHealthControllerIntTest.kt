package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SMOKER_OR_VAPER
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class PrisonerPrisonerHealthControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("PATCH /prisoners/{prisonerNumber}/health")
  @Nested
  inner class SetPrisonerHealthTest {

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
          prisonerNumber = PRISONER_NUMBER,
          requestBody = """{ "smokerOrVaper": 123 }""",
          message = "Validation failure: Couldn't read request body",
        )
      }

      @Test
      fun `bad request when prisoner not found`() {
        expectBadRequestFrom(
          prisonerNumber = "PRISONER_NOT_FOUND",
          requestBody = VALID_REQUEST_BODY,
          message = "Validation failure: Prisoner number 'PRISONER_NOT_FOUND' not found",
        )
      }

      private fun expectBadRequestFrom(prisonerNumber: String, requestBody: String, message: String) {
        webTestClient.patch().uri("/prisoners/$prisonerNumber/health")
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

        expectFieldHistory(
          SMOKER_OR_VAPER,
          HistoryComparison(
            value = SMOKER_NO,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/prisoner_health/health.sql")
      @Sql("classpath:controller/prisoner_health/field_history.sql")
      fun `can update existing health information`() {
        expectFieldHistory(
          SMOKER_OR_VAPER,
          HistoryComparison(
            value = SMOKE_SMOKER,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulUpdateFrom(VALID_REQUEST_BODY).expectBody().json(
          SMOKER_NO_RESPONSE,
          true,
        )

        expectFieldHistory(
          SMOKER_OR_VAPER,
          HistoryComparison(
            value = SMOKE_SMOKER,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = SMOKER_NO,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/prisoner_health/health.sql")
      @Sql("classpath:controller/prisoner_health/field_history.sql")
      fun `can update existing health information to null`() {
        expectFieldHistory(
          SMOKER_OR_VAPER,
          HistoryComparison(
            value = SMOKE_SMOKER,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulUpdateFrom(
          // language=json
          """
            { "smokerOrVaper": null }
          """.trimIndent(),
        ).expectBody().json(
          // language=json
          """
            { "smokerOrVaper": {
                "value":  null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              } 
            }
          """.trimIndent(),
          true,
        )

        expectFieldHistory(
          SMOKER_OR_VAPER,
          HistoryComparison(
            value = SMOKE_SMOKER,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = null,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
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
    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val VALID_REQUEST_BODY =
      // language=json
      """
        { 
          "smokerOrVaper": "SMOKE_NO"
        }
      """.trimIndent()

    val SMOKER_DOMAIN = ReferenceDataDomain(
      code = "SMOKE",
      description = "Smoker or vaper",
      listSequence = 0,
      createdAt = THEN,
      createdBy = "OMS_OWNER",
    )

    val SMOKER_NO = ReferenceDataCode(
      id = "SMOKE_NO",
      domain = SMOKER_DOMAIN,
      code = "NO",
      description = "No, they do not smoke or vape",
      listSequence = 0,
      createdAt = THEN,
      createdBy = "OMS_OWNER",
    )

    val SMOKE_SMOKER = ReferenceDataCode(
      id = "SMOKE_SMOKER",
      domain = SMOKER_DOMAIN,
      code = "SMOKER",
      description = "Yes, they smoke",
      listSequence = 0,
      createdAt = THEN,
      createdBy = "OMS_OWNER",
    )

    val SMOKER_NO_RESPONSE =
      // language=json
      """
      {
        "smokerOrVaper": {
          "value": {
            "id": "SMOKE_NO",
            "description": "No, they do not smoke or vape",
            "listSequence": 0,
            "isActive": true
          },
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1"
        } 
      }
      """.trimIndent()
  }
}