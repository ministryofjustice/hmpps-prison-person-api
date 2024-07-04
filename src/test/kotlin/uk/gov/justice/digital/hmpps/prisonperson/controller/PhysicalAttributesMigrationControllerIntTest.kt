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
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldName.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldName.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldMetadataRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesHistoryRepository
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class PhysicalAttributesMigrationControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @Autowired
  lateinit var physicalAttributesHistoryRepository: PhysicalAttributesHistoryRepository

  @Autowired
  lateinit var fieldMetadataRepository: FieldMetadataRepository

  @DisplayName("PUT /migration/prisoners/{prisonerNumber}/physical-attributes")
  @Nested
  inner class MigratePhysicalAttributesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/physical-attributes")
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
          requestBody = """[{}]""",
          message = "Validation failure: Couldn't read request body",
        )
      }

      private fun expectBadRequestFrom(requestBody: String, message: String) {
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
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
      fun `can migrate single current set of physical attributes`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(SINGLE_CURRENT_RECORD_MIGRATION)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().json(
            // language=json
            """
            {
              "height": 190,
              "weight": 80
            }
            """.trimIndent(),
            false,
          )

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

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can migrate history of physical attributes`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(PHYSICAL_ATTRIBUTES_WITH_HISTORY_MIGRATION)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().json(
            // language=json
            """
            {
              "height": 190,
              "weight": 80
            }
            """.trimIndent(),
            false,
          )

        expectHistory(
          HistoryComparison(
            height = 189,
            weight = 79,
            appliesFrom = NOW.minusDays(1),
            appliesTo = NOW,
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
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

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes_history.sql")
      fun `migration rerun for a prisoner with existing physical attributes data simply overwrites what was there`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(SINGLE_CURRENT_RECORD_MIGRATION)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().json(
            // language=json
            """
            {
              "height": 190,
              "weight": 80
            }
            """.trimIndent(),
            false,
          )

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

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }
    }
  }

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

  private fun expectFieldMetadata(vararg comparison: FieldMetadata) {
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).containsAll(comparison.toList())
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW = ZonedDateTime.of(2024, 6, 14, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val SINGLE_CURRENT_RECORD_MIGRATION =
      // language=json
      """
        [
          { 
            "height": 190,
            "weight": 80,
            "appliesFrom": "2024-06-14T09:10:11.123+01:00[Europe/London]",
            "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
            "createdBy": "USER1"
          }
        ]
      """.trimIndent()

    val PHYSICAL_ATTRIBUTES_WITH_HISTORY_MIGRATION =
      // language=json
      """
        [
          { 
            "height": 190,
            "weight": 80,
            "appliesFrom": "2024-06-14T09:10:11.123+01:00[Europe/London]",
            "createdAt": "2024-06-14T09:10:11.123+01:00[Europe/London]",
            "createdBy": "USER1"
          },
          { 
            "height": 189,
            "weight": 79,
            "appliesFrom": "2024-06-13T09:10:11.123+01:00[Europe/London]",
            "appliesTo": "2024-06-14T09:10:11.123+01:00[Europe/London]",
            "createdAt": "2024-06-13T09:10:11.123+01:00[Europe/London]",
            "createdBy": "USER2"
          }
        ]
      """.trimIndent()

    val VALID_REQUEST_BODY = SINGLE_CURRENT_RECORD_MIGRATION
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
