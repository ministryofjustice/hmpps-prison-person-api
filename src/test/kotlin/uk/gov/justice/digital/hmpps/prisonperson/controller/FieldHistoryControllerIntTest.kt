package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class FieldHistoryControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("GET /prisoners/{prisonerNumber}/field-history/{field}")
  @Nested
  inner class GetFieldHistoryTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/field-history/$FIELD")
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/field-history/$FIELD")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/field-history/$FIELD")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      @Sql(
        "classpath:jpa/repository/reset.sql",
        "classpath:controller/physical_attributes/physical_attributes.sql",
        "classpath:controller/physical_attributes/field_history.sql",
        "classpath:controller/physical_attributes/field_metadata.sql",
      )
      fun `can return field history`() {
        expectSuccessfulGetRequest().expectBody()
          .json(
            // language=json
            """
            [
              {
                "prisonerNumber": "A1234AA",
                "field": "HEIGHT",
                "valueInt": 180,
                "appliesFrom": "2024-01-02T09:10:11+0000",
                "createdAt": "2024-01-02T09:10:11+0000",
                "createdBy": "USER1"
              }
            ]
            """,
          )
      }
    }

    private fun expectSuccessfulGetRequest() =
      webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/field-history/$FIELD")
        .headers(setAuthorisation(USER1, roles = listOf("ROLE_PRISON_PERSON_API__FIELD_HISTORY__RO")))
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isOk
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val FIELD = "HEIGHT"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))
  }
}
