package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
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
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 190, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1, source = NOMIS),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 80, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1, source = NOMIS),
        )
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

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
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 189, appliesFrom = NOW.minusDays(5), appliesTo = NOW.minusDays(3), createdAt = NOW.minusDays(5), createdBy = USER2, source = NOMIS),
          HistoryComparison(value = 188, appliesFrom = NOW.minusDays(3), appliesTo = NOW, createdAt = NOW.minusDays(3), createdBy = USER_CHANGING_HEIGHT_ONLY, source = NOMIS),
          HistoryComparison(value = 190, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1, source = NOMIS),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 79, appliesFrom = NOW.minusDays(5), appliesTo = NOW.minusDays(2), createdAt = NOW.minusDays(5), createdBy = USER2, source = NOMIS),
          HistoryComparison(value = 78, appliesFrom = NOW.minusDays(2), appliesTo = NOW, createdAt = NOW.minusDays(2), createdBy = USER_CHANGING_WEIGHT_ONLY, source = NOMIS),
          HistoryComparison(value = 80, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1, source = NOMIS),
        )
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can migrate nested bookings`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(PHYSICAL_ATTRIBUTES_WITH_NESTED_BOOKINGS)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 189, appliesFrom = NOW.minusYears(2), appliesTo = NOW.minusYears(1), createdAt = NOW.minusYears(2), createdBy = USER2, source = NOMIS),
          HistoryComparison(value = 190, appliesFrom = NOW.minusYears(3), appliesTo = null, createdAt = NOW.minusYears(3), createdBy = USER1, source = NOMIS),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 79, appliesFrom = NOW.minusYears(2), appliesTo = NOW.minusYears(1), createdAt = NOW.minusYears(2), createdBy = USER2, source = NOMIS),
          HistoryComparison(value = 80, appliesFrom = NOW.minusYears(3), appliesTo = null, createdAt = NOW.minusYears(3), createdBy = USER1, source = NOMIS),
        )
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW.minusYears(3), lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW.minusYears(3), lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can migrate booking nested in active booking`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(PHYSICAL_ATTRIBUTES_WITH_BOOKING_NESTED_IN_ACTIVE_BOOKING)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 189, appliesFrom = NOW.minusYears(2), appliesTo = NOW.minusYears(1), createdAt = NOW.minusYears(2), createdBy = USER2, source = NOMIS),
          HistoryComparison(value = 190, appliesFrom = NOW.minusYears(3), appliesTo = null, createdAt = NOW.minusYears(3), createdBy = USER1, source = NOMIS),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 79, appliesFrom = NOW.minusYears(2), appliesTo = NOW.minusYears(1), createdAt = NOW.minusYears(2), createdBy = USER2, source = NOMIS),
          HistoryComparison(value = 80, appliesFrom = NOW.minusYears(3), appliesTo = null, createdAt = NOW.minusYears(3), createdBy = USER1, source = NOMIS),
        )
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW.minusYears(3), lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW.minusYears(3), lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physicalattributes/migration/physical_attributes.sql")
      @Sql("classpath:controller/physicalattributes/migration/field_history.sql")
      fun `migration rerun for a prisoner with existing physical attributes data simply overwrites what was there`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(SINGLE_CURRENT_RECORD_MIGRATION)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 190, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1, source = NOMIS),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 80, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1, source = NOMIS),
        )
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"
    const val USER_CHANGING_HEIGHT_ONLY = "CHANGE_TO_HEIGHT_ONLY"
    const val USER_CHANGING_WEIGHT_ONLY = "CHANGE_TO_WEIGHT_ONLY"

    val NOW = ZonedDateTime.of(2024, 6, 14, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val SINGLE_CURRENT_RECORD_MIGRATION =
      // language=json
      """
        [
          { 
            "height": 190,
            "weight": 80,
            "appliesFrom": "2024-06-14T09:10:11.123+0100",
            "createdAt": "2024-06-14T09:10:11.123+0100",
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
            "appliesFrom": "2024-06-14T09:10:11.123+0100",
            "createdAt": "2024-06-14T09:10:11.123+0100",
            "createdBy": "USER1"
          },
          { 
            "height": 188,
            "weight": 78,
            "appliesFrom": "2024-06-12T09:10:11.123+0100",
            "appliesTo": "2024-06-13T09:10:11.123+0100",
            "createdAt": "2024-06-12T09:10:11.123+0100",
            "createdBy": "CHANGE_TO_WEIGHT_ONLY"
          },
          { 
            "height": 188,
            "weight": 79,
            "appliesFrom": "2024-06-11T09:10:11.123+0100",
            "appliesTo": "2024-06-12T09:10:11.123+0100",
            "createdAt": "2024-06-11T09:10:11.123+0100",
            "createdBy": "CHANGE_TO_HEIGHT_ONLY"
          },
          { 
            "height": 189,
            "weight": 79,
            "appliesFrom": "2024-06-10T09:10:11.123+0100",
            "appliesTo": "2024-06-11T09:10:11.123+0100",
            "createdAt": "2024-06-14T09:10:11.123+0100",
            "createdBy": "NO_CHANGES_MADE"
          },
          { 
            "height": 189,
            "weight": 79,
            "appliesFrom": "2024-06-09T09:10:11.123+0100",
            "appliesTo": "2024-06-10T09:10:11.123+0100",
            "createdAt": "2024-06-09T09:10:11.123+0100",
            "createdBy": "USER2"
          }
        ]
      """.trimIndent()

    val PHYSICAL_ATTRIBUTES_WITH_NESTED_BOOKINGS =
      // language=json
      """
        [
          { 
            "height": 189,
            "weight": 79,
            "appliesFrom": "2022-06-14T09:10:11.123+0100",
            "appliesTo": "2023-06-14T09:10:11.123+0100",
            "createdAt": "2022-06-14T09:10:11.123+0100",
            "createdBy": "USER2"
          },
          { 
            "height": 190,
            "weight": 80,
            "appliesFrom": "2021-06-14T09:10:11.123+0100",
            "appliesTo": "2024-06-14T09:10:11.123+0100",
            "createdAt": "2021-06-14T09:10:11.123+0100",
            "createdBy": "USER1"
          }
        ]
      """.trimIndent()

    val PHYSICAL_ATTRIBUTES_WITH_BOOKING_NESTED_IN_ACTIVE_BOOKING =
      // language=json
      """
        [
          { 
            "height": 189,
            "weight": 79,
            "appliesFrom": "2022-06-14T09:10:11.123+0100",
            "appliesTo": "2023-06-14T09:10:11.123+0100",
            "createdAt": "2022-06-14T09:10:11.123+0100",
            "createdBy": "USER2"
          },
          { 
            "height": 190,
            "weight": 80,
            "appliesFrom": "2021-06-14T09:10:11.123+0100",
            "createdAt": "2021-06-14T09:10:11.123+0100",
            "createdBy": "USER1"
          }
        ]
      """.trimIndent()

    val VALID_REQUEST_BODY = SINGLE_CURRENT_RECORD_MIGRATION
  }
}
