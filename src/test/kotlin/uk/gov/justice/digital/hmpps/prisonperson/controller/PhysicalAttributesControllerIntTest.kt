package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesHistoryRepository
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class PhysicalAttributesControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @Autowired
  lateinit var physicalAttributesHistoryRepository: PhysicalAttributesHistoryRepository

  @DisplayName("PUT /prisoners/{prisonerNumber}/physical-attributes")
  @Nested
  inner class ViewPrisonPersonDataTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
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
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
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
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody().jsonPath("userMessage").isEqualTo(message)
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can create a new set of physical attributes`() {
        expectSuccessfulUpdateFrom("""{ "height": 180, "weight": 70 }""")
          .expectBody().json(
            // language=json
            """
            {
              "height": 180,
              "weight": 70
            }
            """.trimIndent(),
            true,
          )

        expectHistory(
          HistoryComparison(height = 180, weight = 70, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes_history.sql")
      fun `can update an existing set of physical attributes`() {
        expectHistory(
          HistoryComparison(height = 180, weight = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )

        expectSuccessfulUpdateFrom("""{ "height": 181, "weight": 71 }""", user = USER2)
          .expectBody().json(
            // language=json
            """
            {
              "height": 181,
              "weight": 71
            }
            """.trimIndent(),
            true,
          )

        expectHistory(
          HistoryComparison(height = 180, weight = 70, appliesFrom = THEN, appliesTo = NOW, createdAt = THEN, createdBy = USER1),
          HistoryComparison(height = 181, weight = 71, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER2),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes_history.sql")
      fun `can update an existing set of physical attributes a number of times`() {
        expectHistory(
          HistoryComparison(height = 180, weight = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )

        clock.instant = THEN.plusDays(1).toInstant()
        expectSuccessfulUpdateFrom("""{ "height": 181, "weight": 71 }""", user = USER2)

        clock.elapse(Duration.ofDays(1))
        expectSuccessfulUpdateFrom("""{ "height": null, "weight": 72 }""", user = USER1)

        clock.elapse(Duration.ofDays(1))
        expectSuccessfulUpdateFrom("""{ "height": 183, "weight": null }""", user = USER2)

        clock.instant = NOW.toInstant()
        expectSuccessfulUpdateFrom("""{ "height": 183, "weight": 74 }""", user = USER2)

        expectHistory(
          HistoryComparison(height = 180, weight = 70, appliesFrom = THEN, appliesTo = THEN.plusDays(1), createdAt = THEN, createdBy = USER1),
          HistoryComparison(height = 181, weight = 71, appliesFrom = THEN.plusDays(1), appliesTo = THEN.plusDays(2), createdAt = THEN.plusDays(1), createdBy = USER2),
          HistoryComparison(height = null, weight = 72, appliesFrom = THEN.plusDays(2), appliesTo = THEN.plusDays(3), createdAt = THEN.plusDays(2), createdBy = USER1),
          HistoryComparison(height = 183, weight = null, appliesFrom = THEN.plusDays(3), appliesTo = NOW, createdAt = THEN.plusDays(3), createdBy = USER2),
          HistoryComparison(height = 183, weight = 74, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER2),
        )
      }

      private fun expectSuccessfulUpdateFrom(requestBody: String, user: String? = USER1) =
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(user, roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .exchange()
          .expectStatus().isOk

      private fun expectHistory(vararg comparison: HistoryComparison) {
        val history = physicalAttributesHistoryRepository.findAllByPhysicalAttributesPrisonerNumber(PRISONER_NUMBER).toList()
        assertThat(history).hasSize(comparison.size)

        history.forEachIndexed { index, actual ->
          val expected = comparison[index]
          assertThat(actual.height).isEqualTo(expected.height)
          assertThat(actual.weight).isEqualTo(expected.weight)
          assertThat(actual.appliesFrom).isEqualTo(expected.appliesFrom)
          assertThat(actual.appliesTo).isEqualTo(expected.appliesTo)
          assertThat(actual.createdAt).isEqualTo(expected.createdAt)
          assertThat(actual.createdBy).isEqualTo(expected.createdBy)
        }
      }
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))
  }

  private data class HistoryComparison(
    val height: Int?,
    val weight: Int?,
    val appliesFrom: ZonedDateTime,
    val appliesTo: ZonedDateTime?,
    val createdAt: ZonedDateTime,
    val createdBy: String,
  )
}
