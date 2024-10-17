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
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonFieldInformation
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class ProfileDetailsPhysicalAttributesSyncControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("PUT /sync/prisoners/{prisonerNumber}/profile-details-physical-attributes")
  @Nested
  inner class SyncProfileDetailsPhysicalAttributesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
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
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNC__RW")))
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
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES)
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PRISONER_HAIR,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )
        expectNoFieldHistoryFor(
          HEIGHT,
          WEIGHT,
          FACIAL_HAIR,
          FACE,
          BUILD,
          LEFT_EYE_COLOUR,
          RIGHT_EYE_COLOUR,
          SHOE_SIZE,
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes/profile_details_sync/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes/profile_details_sync/field_history.sql")
      fun `can sync an update of existing physical attributes`() {
        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PREVIOUS_PRISONER_HAIR,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )
        expectNoFieldHistoryFor(
          HEIGHT,
          WEIGHT,
          FACIAL_HAIR,
          FACE,
          BUILD,
          LEFT_EYE_COLOUR,
          RIGHT_EYE_COLOUR,
          SHOE_SIZE,
        )

        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES)
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PREVIOUS_PRISONER_HAIR,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
            source = DPS,
          ),
          HistoryComparison(
            value = PRISONER_HAIR,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes/profile_details_sync/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes/profile_details_sync/field_history.sql")
      fun `can sync an update of the existing physical attributes when the booking has an end date`() {
        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PREVIOUS_PRISONER_HAIR,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )
        expectNoFieldHistoryFor(
          HEIGHT,
          WEIGHT,
          FACIAL_HAIR,
          FACE,
          BUILD,
          LEFT_EYE_COLOUR,
          RIGHT_EYE_COLOUR,
          SHOE_SIZE,
        )

        expectSuccessfulSyncFrom(
          REQUEST_TO_SYNC_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_FOR_LATEST_BOOKING_WITH_END_DATE,
        )
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PREVIOUS_PRISONER_HAIR,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
            source = DPS,
          ),
          HistoryComparison(
            value = PRISONER_HAIR,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physical_attributes/profile_details_sync/physical_attributes.sql")
      @Sql("classpath:controller/physical_attributes/profile_details_sync/field_history.sql")
      fun `can sync a historical update of physical attributes`() {
        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PREVIOUS_PRISONER_HAIR,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_HISTORICAL_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES)
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PRISONER_HAIR,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
          HistoryComparison(
            value = PREVIOUS_PRISONER_HAIR,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
            source = DPS,
          ),
        )
        expectNoFieldHistoryFor(
          HEIGHT,
          WEIGHT,
          FACIAL_HAIR,
          FACE,
          BUILD,
          LEFT_EYE_COLOUR,
          RIGHT_EYE_COLOUR,
          SHOE_SIZE,
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can sync creation of historical physical attributes`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_HISTORICAL_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES)
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PRISONER_HAIR,
            appliesFrom = THEN.minusYears(1),
            appliesTo = NOW.minusYears(1),
            createdAt = NOW,
            createdBy = USER1,
            source = NOMIS,
          ),
        )
        expectNoFieldHistoryFor(
          HEIGHT,
          WEIGHT,
          FACIAL_HAIR,
          FACE,
          BUILD,
          LEFT_EYE_COLOUR,
          RIGHT_EYE_COLOUR,
          SHOE_SIZE,
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `should publish domain event`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES)

        await untilCallTo { publishTestQueue.countAllMessagesOnQueue() } matches { it == 1 }
        val event = publishTestQueue.receiveDomainEventOnQueue<PrisonPersonFieldInformation>()

        val expected = DomainEvent(
          eventType = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.type,
          additionalInformation = PrisonPersonFieldInformation(
            url = "http://localhost:8080/prisoners/${PRISONER_NUMBER}",
            prisonerNumber = PRISONER_NUMBER,
            source = NOMIS,
            fields = listOf(HAIR),
          ),
          description = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.description,
          occurredAt = NOW,
        )

        assertThat(event).isEqualTo(expected)
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `should publish telemetry event`() {
        expectSuccessfulSyncFrom(REQUEST_TO_SYNC_LATEST_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES)

        verify(telemetryClient).trackEvent(
          eq("prison-person-api-profile-details-physical-attributes-synced"),
          argThat { it ->
            it["prisonerNumber"] == PRISONER_NUMBER &&
              it["source"] == NOMIS.name &&
              it["fields"] == listOf(HAIR.name).toString()
          },
          isNull(),
        )
      }

      private fun expectSuccessfulSyncFrom(requestBody: String) =
        webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNC__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .exchange()
          .expectStatus().isOk
    }
  }

  @Nested
  inner class ErrorConditions {

    @Test
    @Sql("classpath:jpa/repository/reset.sql")
    @Sql("classpath:controller/physical_attributes/profile_details_sync/physical_attributes.sql")
    @Sql("classpath:controller/physical_attributes/profile_details_sync/field_history.sql")
    fun `returns 500 if history would have illogical applies_to and applies_from timestamps`() {
      webTestClient.put().uri("/sync/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
        .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNC__RW")))
        .header("Content-Type", "application/json")
        .bodyValue(
          // language=json
          """
            {
              "hair": {
                  "value": "BLONDE",
                  "lastModifiedAt": "1990-01-01T09:10:11.123+0100",
                  "lastModifiedBy": "USER1"
                },
               "appliesFrom": "2024-06-14T09:10:11.123+0100"
            }
          """.trimIndent(),
        )
        .exchange()
        .expectStatus().is5xxServerError
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val PRISONER_HAIR = generateRefDataCode("BLONDE", "Blonde", "HAIR")
    val PREVIOUS_PRISONER_HAIR = generateRefDataCode("BLACK", "Black", "HAIR")

    fun generateRefDataCode(code: String, desc: String, domain: String): ReferenceDataCode {
      val rdd = ReferenceDataDomain(domain, "Hair type or colour", 0, ZonedDateTime.now(), "testUser")
      return ReferenceDataCode("${domain}_$code", code, rdd, desc, 0, ZonedDateTime.now(), "testUser")
    }

    val REQUEST_TO_SYNC_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_FOR_LATEST_BOOKING_WITH_END_DATE =
      // language=json
      """
        {
            "hair": {
                "value": "BLONDE",
                "lastModifiedAt": "2024-06-14T09:10:11.123+0100",
                "lastModifiedBy": "USER1"
            },
           "appliesFrom": "2023-01-02T09:10:11.123+0000",
           "appliesTo": "2024-06-14T09:10:11.123+0100",
           "latestBooking": true
        }
      """.trimIndent()

    val REQUEST_TO_SYNC_HISTORICAL_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES =
      // language=json
      """
        {
            "hair": {
                "value": "BLONDE",
                "lastModifiedAt": "2024-06-14T09:10:11.123+0100",
                "lastModifiedBy": "USER1"
            },
           "appliesFrom": "2023-01-02T09:10:11.123+0000",
           "appliesTo": "2023-06-14T09:10:11.123+0100",
           "latestBooking": false
        }
      """.trimIndent()

    val REQUEST_TO_SYNC_LATEST_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES =
      // language=json
      """
        {
            "hair": {
                "value": "BLONDE",
                "lastModifiedAt": "2024-06-14T09:10:11.123+0100",
                "lastModifiedBy": "USER1"
            },
           "appliesFrom": "2023-01-02T09:10:11.123+0000"
        }
      """.trimIndent()

    val VALID_REQUEST_BODY = REQUEST_TO_SYNC_LATEST_PROFILE_DETAILS_PHYSICAL_ATTRIBUTES
  }
}
