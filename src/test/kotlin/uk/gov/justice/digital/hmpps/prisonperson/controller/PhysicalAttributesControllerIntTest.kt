package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
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
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonAdditionalInformation
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

  @DisplayName("GET /prisoners/{prisonerNumber}/physical-attributes")
  @Nested
  inner class GetPhysicalAttributesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
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
        webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(USER1, roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      @Sql(
        "classpath:jpa/repository/reset.sql",
        "classpath:controller/physicalattributes/physical_attributes.sql",
        "classpath:controller/physicalattributes/field_history.sql",
        "classpath:controller/physicalattributes/field_metadata.sql",
      )
      fun `can return physical attributes`() {
        expectSuccessfulGetRequest().expectBody()
          .json(
            // language=json
            """
            { 
              "height": {
                "value":180,
                "lastModifiedAt":"2024-01-02T09:10:11+0000",
                "lastModifiedBy":"USER1"
              },
              "weight": {
                "value":70,
                "lastModifiedAt":"2024-01-02T09:10:11+0000",
                "lastModifiedBy":"USER1"
              },
              "hair": null,
              "facialHair": null,
              "face": null,
              "build": null,
              "leftEyeColour": null,
              "rightEyeColour": null,
              "shoeSize": null
            }
            """,
          )
      }
    }

    private fun expectSuccessfulGetRequest() =
      webTestClient.get().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
        .headers(setAuthorisation(USER1, roles = listOf("ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO")))
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isOk
  }

  @DisplayName("PATCH /prisoners/{prisonerNumber}/physical-attributes")
  @Nested
  inner class SetPhysicalAttributesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
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
      fun `bad request when field type is not as expected`() {
        expectBadRequestFrom(
          requestBody = """{ "hair": 123 }""",
          message = "Validation failure: Couldn't read request body",
        )
      }

      @Test
      fun `bad request when height below 30cm`() {
        expectBadRequestFrom(
          requestBody = """{ "height": 29 }""",
          message = "Validation failure(s): The height must be a plausible value in centimetres (between 30 and 274), null or not provided",
        )
      }

      @Test
      fun `bad request when height exceeds 274cm`() {
        expectBadRequestFrom(
          requestBody = """{ "height": 275 }""",
          message = "Validation failure(s): The height must be a plausible value in centimetres (between 30 and 274), null or not provided",
        )
      }

      @Test
      fun `bad request when weight below 12kg`() {
        expectBadRequestFrom(
          requestBody = """{ "weight": 11 }""",
          message = "Validation failure(s): The weight must be a plausible value in kilograms (between 12 and 635), null or not provided",
        )
      }

      @Test
      fun `bad request when weight exceeds 635kg`() {
        expectBadRequestFrom(
          requestBody = """{ "weight": 636 }""",
          message = "Validation failure(s): The weight must be a plausible value in kilograms (between 12 and 635), null or not provided",
        )
      }

      @Test
      fun `bad request when prisoner not found`() {
        webTestClient.patch().uri("/prisoners/PRISONER_NOT_FOUND/physical-attributes")
          .headers(setAuthorisation(USER1, roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody().jsonPath("userMessage")
          .isEqualTo("Validation failure: Prisoner number 'PRISONER_NOT_FOUND' not found")
      }

      private fun expectBadRequestFrom(requestBody: String, message: String) {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
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
        expectSuccessfulUpdateFrom("""{ "height": 180 }""")
          .expectBody().json(
            // language=json
            """
            {
              "height": {
                "value":180,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "weight": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "hair": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "facialHair": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "face": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "build": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "leftEyeColour": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "rightEyeColour": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "shoeSize": {
                "value":null,
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              }
            }
            """.trimIndent(),
            true,
          )

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER1),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, BUILD, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, LEFT_EYE_COLOUR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, RIGHT_EYE_COLOUR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, FACE, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, FACIAL_HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, SHOE_SIZE, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physicalattributes/physical_attributes.sql")
      @Sql("classpath:controller/physicalattributes/field_history.sql")
      fun `can update an existing set of physical attributes`() {
        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = HAIR_BLACK,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          SHOE_SIZE,
          HistoryComparison(value = null, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )

        expectSuccessfulUpdateFrom(
          """{ "height": 181, "weight": 71, "hair": "HAIR_GREY", "shoeSize": "11" }""",
          user = USER2,
        )
          .expectBody().json(
            // language=json
            """
            {
              "height": {
                "value": 181,
                "lastModifiedAt": "2024-06-14T09:10:11+0100",
                "lastModifiedBy": "USER2"
              },
              "weight": {
                "value": 71,
                "lastModifiedAt": "2024-06-14T09:10:11+0100",
                "lastModifiedBy": "USER2"
              },
              "hair": {
                "value": {
                  "id": "HAIR_GREY",
                  "description": "Grey",
                  "listSequence": 0,
                  "isActive": true
                },
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER2" 
              },
              "facialHair": null,
              "face": null,
              "build": null,
              "leftEyeColour": null,
              "rightEyeColour": null,
              "shoeSize": {
                "value": "11",
                "lastModifiedAt": "2024-06-14T09:10:11+0100",
                "lastModifiedBy": "USER2"
              }
            }
            """.trimIndent(),
            true,
          )

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = THEN, appliesTo = NOW, createdAt = THEN, createdBy = USER1),
          HistoryComparison(value = 181, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER2),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 70, appliesFrom = THEN, appliesTo = NOW, createdAt = THEN, createdBy = USER1),
          HistoryComparison(value = 71, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER2),
        )

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = HAIR_BLACK,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = HAIR_GREY,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER2,
          ),
        )

        expectFieldHistory(
          SHOE_SIZE,
          HistoryComparison(
            value = null,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = "11",
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER2,
          ),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER2),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER2),
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER2),
          FieldMetadata(PRISONER_NUMBER, SHOE_SIZE, lastModifiedAt = NOW, lastModifiedBy = USER2),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:controller/physicalattributes/physical_attributes.sql")
      @Sql("classpath:controller/physicalattributes/field_history.sql")
      fun `can update an existing set of physical attributes a number of times`() {
        expectFieldHistory(
          HEIGHT,
          HistoryComparison(value = 180, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          WEIGHT,
          HistoryComparison(value = 70, appliesFrom = THEN, appliesTo = null, createdAt = THEN, createdBy = USER1),
        )
        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = HAIR_BLACK,
            appliesFrom = THEN,
            appliesTo = null,
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        clock.instant = THEN.plusDays(1).toInstant()
        expectSuccessfulUpdateFrom(
          """{ "height": 181, "weight": 71, "hair": "HAIR_GREY", "shoeSize": "9.5" }""",
          user = USER2,
        )

        clock.elapse(Duration.ofDays(1))
        expectSuccessfulUpdateFrom("""{ "height": null, "weight": 72, "hair": null, "shoeSize": null }""", user = USER1)

        clock.elapse(Duration.ofDays(1))
        expectSuccessfulUpdateFrom(
          """{ "height": 183, "weight": null, "hair": "HAIR_BLONDE", "shoeSize": "10" }""",
          user = USER2,
        )

        clock.instant = NOW.toInstant()
        expectSuccessfulUpdateFrom(
          """{ "height": 183, "weight": 74, "hair": "HAIR_GREY", "shoeSize": "11" }""",
          user = USER2,
        )

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = 180,
            appliesFrom = THEN,
            appliesTo = THEN.plusDays(1),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = 181,
            appliesFrom = THEN.plusDays(1),
            appliesTo = THEN.plusDays(2),
            createdAt = THEN.plusDays(1),
            createdBy = USER2,
          ),
          HistoryComparison(
            value = null,
            appliesFrom = THEN.plusDays(2),
            appliesTo = THEN.plusDays(3),
            createdAt = THEN.plusDays(2),
            createdBy = USER1,
          ),
          HistoryComparison(
            value = 183,
            appliesFrom = THEN.plusDays(3),
            appliesTo = null,
            createdAt = THEN.plusDays(3),
            createdBy = USER2,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = 70,
            appliesFrom = THEN,
            appliesTo = THEN.plusDays(1),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = 71,
            appliesFrom = THEN.plusDays(1),
            appliesTo = THEN.plusDays(2),
            createdAt = THEN.plusDays(1),
            createdBy = USER2,
          ),
          HistoryComparison(
            value = 72,
            appliesFrom = THEN.plusDays(2),
            appliesTo = THEN.plusDays(3),
            createdAt = THEN.plusDays(2),
            createdBy = USER1,
          ),
          HistoryComparison(
            value = null,
            appliesFrom = THEN.plusDays(3),
            appliesTo = NOW,
            createdAt = THEN.plusDays(3),
            createdBy = USER2,
          ),
          HistoryComparison(value = 74, appliesFrom = NOW, appliesTo = null, createdAt = NOW, createdBy = USER2),
        )

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = HAIR_BLACK,
            appliesFrom = THEN,
            appliesTo = THEN.plusDays(1),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = HAIR_GREY,
            appliesFrom = THEN.plusDays(1),
            appliesTo = THEN.plusDays(2),
            createdAt = THEN.plusDays(1),
            createdBy = USER2,
          ),
          HistoryComparison(
            value = null,
            appliesFrom = THEN.plusDays(2),
            appliesTo = THEN.plusDays(3),
            createdAt = THEN.plusDays(2),
            createdBy = USER1,
          ),
          HistoryComparison(
            value = HAIR_BLONDE,
            appliesFrom = THEN.plusDays(3),
            appliesTo = NOW,
            createdAt = THEN.plusDays(3),
            createdBy = USER2,
          ),
          HistoryComparison(
            value = HAIR_GREY,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER2,
          ),
        )

        expectFieldHistory(
          SHOE_SIZE,
          HistoryComparison(
            value = null,
            appliesFrom = THEN,
            appliesTo = THEN.plusDays(1),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = "9.5",
            appliesFrom = THEN.plusDays(1),
            appliesTo = THEN.plusDays(2),
            createdAt = THEN.plusDays(1),
            createdBy = USER2,
          ),
          HistoryComparison(
            value = null,
            appliesFrom = THEN.plusDays(2),
            appliesTo = THEN.plusDays(3),
            createdAt = THEN.plusDays(2),
            createdBy = USER1,
          ),
          HistoryComparison(
            value = "10",
            appliesFrom = THEN.plusDays(3),
            appliesTo = NOW,
            createdAt = THEN.plusDays(3),
            createdBy = USER2,
          ),
          HistoryComparison(
            value = "11",
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER2,
          ),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HEIGHT, lastModifiedAt = THEN.plusDays(3), lastModifiedBy = USER2),
          FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER2),
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER2),
          FieldMetadata(PRISONER_NUMBER, SHOE_SIZE, lastModifiedAt = NOW, lastModifiedBy = USER2),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `should publish domain event`() {
        expectSuccessfulUpdateFrom("""{ "height": 180, "weight": 70 }""")

        await untilCallTo { publishTestQueue.countAllMessagesOnQueue() } matches { it == 1 }
        val event = publishTestQueue.receiveDomainEventOnQueue<PrisonPersonAdditionalInformation>()

        assertThat(event).isEqualTo(
          DomainEvent(
            eventType = PHYSICAL_ATTRIBUTES_UPDATED.domainEventType,
            additionalInformation = PrisonPersonAdditionalInformation(
              url = "http://localhost:8080/prisoners/${PRISONER_NUMBER}",
              prisonerNumber = PRISONER_NUMBER,
              source = DPS,
            ),
            description = PHYSICAL_ATTRIBUTES_UPDATED.description,
            occurredAt = ZonedDateTime.now(clock),
          ),
        )
      }

      private fun expectSuccessfulUpdateFrom(requestBody: String, user: String? = USER1) =
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}/physical-attributes")
          .headers(setAuthorisation(user, roles = listOf("ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .exchange()
          .expectStatus().isOk
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val VALID_REQUEST_BODY =
      // language=json
      """
        { 
          "height": 180,
          "weight": 70
        }
      """.trimIndent()

    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val HAIR_DOMAIN =
      ReferenceDataDomain("HAIR", "Hair type or colour", 0, THEN, "OMS_OWNER")
    val HAIR_BLACK = ReferenceDataCode(
      id = "HAIR_BLACK",
      domain = HAIR_DOMAIN,
      code = "BLACK",
      description = "Black",
      listSequence = 0,
      createdAt = THEN,
      createdBy = "OMS_OWNER",
    )
    val HAIR_BLONDE = ReferenceDataCode(
      id = "HAIR_BLONDE",
      domain = HAIR_DOMAIN,
      code = "BLONDE",
      description = "Blonde",
      listSequence = 0,
      createdAt = THEN,
      createdBy = "OMS_OWNER",
    )
    val HAIR_GREY = ReferenceDataCode(
      id = "HAIR_GREY",
      domain = HAIR_DOMAIN,
      code = "GREY",
      description = "Grey",
      listSequence = 0,
      createdAt = THEN,
      createdBy = "OMS_OWNER",
    )
  }
}
