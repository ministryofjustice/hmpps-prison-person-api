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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
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
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PRISON_ID
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonFieldInformation
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
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes/sync/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes/sync/field_history.sql")
      fun `can sync an update of existing physical attributes`() {
        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

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
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes/sync/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes/sync/field_history.sql")
      fun `can sync an update of the existing physical attributes when the booking has an end date`() {
        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)

        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_PHYSICAL_ATTRIBUTES_FOR_LATEST_BOOKING_WITH_END_DATE)
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
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes/sync/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes/sync/field_history.sql")
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
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)
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
        expectNoFieldHistoryFor(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes/sync/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes/sync/field_history.sql")
      fun `adds anomalous flag if history would have illogical applies_to and applies_from timestamps`() {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(
            // language=json
            """
          { 
            "height": 200,
            "weight": 100,
            "appliesFrom": "2024-06-14T09:10:11.123+0100",
            "createdAt": "1990-01-01T09:10:11.123+0100",
            "createdBy": "USER1",
            "latestBooking": false
          }
            """.trimIndent(),
          )
          .exchange()
          .expectStatus().isOk

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = 200,
            appliesFrom = ZonedDateTime.parse("2024-06-14T09:10:11.123+01:00"),
            appliesTo = null,
            createdAt = ZonedDateTime.parse("1990-01-01T09:10:11.123+01:00"),
            createdBy = USER1,
            source = NOMIS,
            anomalous = true,
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
            value = 100,
            appliesFrom = ZonedDateTime.parse("2024-06-14T09:10:11.123+01:00"),
            appliesTo = null,
            createdAt = ZonedDateTime.parse("1990-01-01T09:10:11.123+01:00"),
            createdBy = USER1,
            source = NOMIS,
            anomalous = true,
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
      fun `should publish domain event`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES)

        await untilCallTo { publishTestQueue.countAllMessagesOnQueue() } matches { it == 1 }
        val event = publishTestQueue.receiveDomainEventOnQueue<PrisonPersonFieldInformation>()

        val expected = DomainEvent(
          eventType = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.type,
          additionalInformation = PrisonPersonFieldInformation(
            url = "http://localhost:8080/prisoners/${PRISONER_NUMBER}",
            prisonerNumber = PRISONER_NUMBER,
            source = NOMIS,
            fields = listOf(HEIGHT, WEIGHT),
          ),
          description = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.description,
          occurredAt = NOW,
        )

        assertThat(event).isEqualTo(expected)
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `should publish telemetry event`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES)

        verify(telemetryClient).trackEvent(
          eq("prison-person-api-physical-attributes-synced"),
          argThat { it ->
            it["prisonerNumber"] == PRISONER_NUMBER &&
              it["source"] == NOMIS.name &&
              it["fields"] == listOf(HEIGHT.name, WEIGHT.name).toString() &&
              it["prisonId"] == PRISON_ID
          },
          isNull(),
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

  @DisplayName("GET /sync/prisoners/{prisonerNumber}/physical-attributes")
  @Nested
  inner class GetPhysicalAttributesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `not found response returned when there are no physical attributes recorded yet for the prisoner`() {
        webTestClient.get().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(USER1, roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      @Sql(
        "classpath:jpa/repository/reset.sql",
        "classpath:controller/physical_attributes/migration/physical_attributes.sql",
        "classpath:controller/physical_attributes/migration/field_history.sql",
        "classpath:controller/physical_attributes/migration/field_metadata.sql",
      )
      fun `can return physical attributes`() {
        expectSuccessfulGetRequest().expectBody()
          .json(
            // language=json
            """
            { 
              "height": 180,
              "weight": 70,
              "hair": "BLACK",
              "facialHair": "SIDEBURNS",
              "face": "ROUND",
              "build": "HEAVY",
              "leftEyeColour": "BROWN",
              "rightEyeColour": "BROWN",
              "shoeSize": "9"
            }
            """,
          )
      }
    }

    private fun expectSuccessfulGetRequest() =
      webTestClient.get().uri("/sync/prisoners/${PRISONER_NUMBER}/physical-attributes")
        .headers(setAuthorisation(USER1, roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW")))
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isOk
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val REQUEST_TO_SYNC_PHYSICAL_ATTRIBUTES_FOR_LATEST_BOOKING_WITH_END_DATE =
      // language=json
      """
        { 
          "height": 190,
          "weight": 80,
          "appliesFrom": "2023-01-02T09:10:11.123+0000",
          "appliesTo": "2023-06-14T09:10:11.123+0100",
          "createdAt": "2024-06-14T09:10:11.123+0100",
          "createdBy": "USER1",
          "latestBooking": true 
        }
      """.trimIndent()

    val REQUEST_TO_SYNC_HISTORICAL_PHYSICAL_ATTRIBUTES =
      // language=json
      """
        { 
          "height": 190,
          "weight": 80,
          "appliesFrom": "2023-01-02T09:10:11.123+0000",
          "appliesTo": "2023-06-14T09:10:11.123+0100",
          "createdAt": "2024-06-14T09:10:11.123+0100",
          "createdBy": "USER1",
          "latestBooking": false
        }
      """.trimIndent()

    val REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES =
      // language=json
      """
        { 
          "height": 190,
          "weight": 80,
          "appliesFrom": "2024-06-14T09:10:11.123+0100",
          "createdAt": "2024-06-14T09:10:11.123+0100",
          "createdBy": "USER1",
          "latestBooking": true
        }
      """.trimIndent()

    val VALID_REQUEST_BODY = REQUEST_TO_SYNC_LATEST_PHYSICAL_ATTRIBUTES
  }
}
