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
import java.time.ZoneId
import java.time.ZonedDateTime

class PhysicalAttributesSyncControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @Autowired
  lateinit var physicalAttributesHistoryRepository: PhysicalAttributesHistoryRepository

  @DisplayName("PUT /sync/prisoners/{prisonerNumber}/physical-attributes")
  @Nested
  inner class SyncPhysicalAttributesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    @Sql("classpath:jpa/repository/reset.sql")
    inner class Validation {

      @Test
      fun `bad request when required fields are missing`() {
        expectBadRequestFrom(
          requestBody = """{}""",
          message = "Validation failure: Couldn't read request body",
        )
      }

      private fun expectBadRequestFrom(requestBody: String, message: String) {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW")))
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
      fun `can sync creation of a new set of physical attributes`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES)
          .expectBody().json(
            // language=json
            """
            {
              "height": 190,
              "weight": 80,
              "appliesFrom": "2024-06-14T09:10:11.123+01:00[Europe/London]",
              "appliesTo": null,
              "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
              "createdBy": "USER1"
            }
            """.trimIndent(),
            false,
          )
          .jsonPath("physicalAttributesHistoryId").isNumber

        expectHistory(
          HistoryComparison(
            height = 190,
            weight = 80,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes_history.sql")
      fun `can sync an update of existing physical attributes`() {
        expectHistory(
          HistoryComparison(
            height = 180,
            weight = 70,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES)
          .expectBody().json(
            // language=json
            """
            {
              "height": 190,
              "weight": 80,
              "appliesFrom": "2024-06-14T09:10:11.123+01:00[Europe/London]",
              "appliesTo": null,
              "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
              "createdBy": "USER1"
            }
            """.trimIndent(),
            false,
          )
          .jsonPath("physicalAttributesHistoryId").isNumber

        expectHistory(
          HistoryComparison(
            height = 180,
            weight = 70,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            height = 190,
            weight = 80,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes_history.sql")
      fun `can sync a historical update of physical attributes`() {
        expectHistory(
          HistoryComparison(
            height = 180,
            weight = 70,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_HISTORICAL_PHYSICAL_ATTRIBUTES)
          .expectBody().json(
            // language=json
            """
            {
              "height": 190,
              "weight": 80,
              "appliesFrom": "2023-01-02T09:10:11.123Z[Europe/London]",
              "appliesTo": "2023-06-14T09:10:11.123+01:00[Europe/London]",
              "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
              "createdBy": "USER1"
            }
            """.trimIndent(),
            false,
          )
          .jsonPath("physicalAttributesHistoryId").isNumber

        expectHistory(
          HistoryComparison(
            height = 190,
            weight = 80,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
          ),
          HistoryComparison(
            height = 180,
            weight = 70,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can sync creation of historical physical attributes`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_HISTORICAL_PHYSICAL_ATTRIBUTES)
          .expectBody().json(
            // language=json
            """
            {
              "height": 190,
              "weight": 80,
              "appliesFrom": "2023-01-02T09:10:11.123Z[Europe/London]",
              "appliesTo": "2023-06-14T09:10:11.123+01:00[Europe/London]",
              "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
              "createdBy": "USER1"
            }
            """.trimIndent(),
            false,
          )
          .jsonPath("physicalAttributesHistoryId").isNumber

        expectHistory(
          HistoryComparison(
            height = 190,
            weight = 80,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      private fun expectSuccessfulSyncFrom(requestBody: String) =
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .exchange()
          .expectStatus().isOk

      private fun expectHistory(vararg comparison: HistoryComparison) {
        val history = physicalAttributesHistoryRepository.findAllByPhysicalAttributesPrisonerNumber(
          PRISONER_NUMBER,
        ).toList()
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

    val NOW = ZonedDateTime.of(2024, 6, 14, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val REQUEST_TO_SYNC_HISTORICAL_PHYSICAL_ATTRIBUTES =
      // language=json
      """
        { 
          "height": 190,
          "weight": 80,
          "appliesFrom": "2023-01-02T09:10:11.123+00:00[Europe/London]",
          "appliesTo": "2023-06-14T09:10:11.123+01:00[Europe/London]",
          "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
          "createdBy": "USER1"
        }
      """.trimIndent()

    val REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES =
      // language=json
      """
        { 
          "height": 190,
          "weight": 80,
          "appliesFrom": "2024-06-14T09:10:11.123+01:00[Europe/London]",
          "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
          "createdBy": "USER1"
        }
      """.trimIndent()

    val VALID_REQUEST_BODY = REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES
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
