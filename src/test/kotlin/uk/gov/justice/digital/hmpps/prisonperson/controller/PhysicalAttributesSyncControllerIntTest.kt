package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonAdditionalInformation
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
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = 190,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = 80,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, FACIAL_HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, FACE, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, BUILD, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, LEFT_EYE_COLOUR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, RIGHT_EYE_COLOUR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, SHOE_SIZE, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes_history.sql")
      fun `can sync an update of existing physical attributes`() {
        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )

        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES)
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = 180,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
            source = DPS,
          ),
          HistoryComparison(
            value = 190,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = 70,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
            source = DPS,
          ),
          HistoryComparison(
            value = 80,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes_history.sql")
      fun `can sync a historical update of physical attributes`() {
        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )

        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_HISTORICAL_PHYSICAL_ATTRIBUTES)
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = 190,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
          HistoryComparison(
            value = 180,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
            source = DPS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = 80,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
          HistoryComparison(
            value = 70,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
            source = DPS,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can sync creation of historical physical attributes`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_HISTORICAL_PHYSICAL_ATTRIBUTES)
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = 190,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = 80,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `should publish domain event`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES)

        await untilCallTo { publishTestQueue.countAllMessagesOnQueue() } matches { it == 1 }
        val event = publishTestQueue.receiveDomainEventOnQueue<PrisonPersonAdditionalInformation>()

        assertThat(event).isEqualTo(
          DomainEvent(
            eventType = PHYSICAL_ATTRIBUTES_UPDATED.domainEventType,
            additionalInformation = PrisonPersonAdditionalInformation(
              url = "http://localhost:8080/prisoners/${PRISONER_NUMBER}",
              prisonerNumber = PRISONER_NUMBER,
              source = NOMIS,
            ),
            description = PHYSICAL_ATTRIBUTES_UPDATED.description,
            occurredAt = NOW,
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
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    val NOW = ZonedDateTime.now(clock)
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
}
