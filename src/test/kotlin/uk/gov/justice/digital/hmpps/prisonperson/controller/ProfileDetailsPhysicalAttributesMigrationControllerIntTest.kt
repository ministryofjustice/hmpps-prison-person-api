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
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class ProfileDetailsPhysicalAttributesMigrationControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("PUT /migration/prisoners/{prisonerNumber}/profile-details-physical-attributes")
  @Nested
  inner class MigrateProfileDetailsPhysicalAttributesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
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
        webTestClient.put().uri("/migration/prisoners/${PRISONER_NUMBER}/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
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
      fun `can migrate single current set of profile details physical attributes`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(SINGLE_CURRENT_RECORD_MIGRATION)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = generateRefDataCode(prisonerHair),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          FACIAL_HAIR,
          HistoryComparison(
            value = generateRefDataCode(prisonerFacialHair),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          FACE,
          HistoryComparison(
            value = generateRefDataCode(prisonerFace),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          BUILD,
          HistoryComparison(
            value = generateRefDataCode(prisonerBuild),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          LEFT_EYE_COLOUR,
          HistoryComparison(
            value = generateRefDataCode(prisonerLeftEyeColour),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          RIGHT_EYE_COLOUR,
          HistoryComparison(
            value = generateRefDataCode(prisonerRightEyeColour),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          SHOE_SIZE,
          HistoryComparison(
            value = PRISONER_SHOE_SIZE,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectNoFieldHistoryFor(HEIGHT, WEIGHT)

        expectFieldMetadata(
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
      fun `can migrate history of profile details physical attributes`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(PHYSICAL_ATTRIBUTES_WITH_HISTORY_MIGRATION)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = generateRefDataCode(prisonerHairPrevious),
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(2),
            appliesTo = NOW.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(prisonerHair),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          FACIAL_HAIR,
          HistoryComparison(
            value = generateRefDataCode(prisonerFacialHairPrevious),
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(2),
            appliesTo = NOW.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(prisonerFacialHair),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          FACE,
          HistoryComparison(
            value = generateRefDataCode(prisonerFacePrevious),
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(2),
            appliesTo = NOW.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(prisonerFace),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          BUILD,
          HistoryComparison(
            value = generateRefDataCode(prisonerBuildPrevious),
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(2),
            appliesTo = NOW.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(prisonerBuild),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          LEFT_EYE_COLOUR,
          HistoryComparison(
            value = generateRefDataCode(prisonerLeftEyeColourPrevious),
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(2),
            appliesTo = NOW.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(prisonerLeftEyeColour),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          RIGHT_EYE_COLOUR,
          HistoryComparison(
            value = generateRefDataCode(prisonerRightEyeColourPrevious),
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(2),
            appliesTo = NOW.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(prisonerRightEyeColour),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          SHOE_SIZE,
          HistoryComparison(
            value = PRISONER_SHOE_SIZE_PREVIOUS,
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(2),
            appliesTo = NOW.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = PRISONER_SHOE_SIZE,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectNoFieldHistoryFor(HEIGHT, WEIGHT)

        expectFieldMetadata(
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
      @Sql("classpath:controller/physicalattributes/migration/physical_attributes.sql")
      @Sql("classpath:controller/physicalattributes/migration/field_history.sql")
      fun `migration rerun for a prisoner with existing profile details physical attributes data simply overwrites what was there`() {
        webTestClient.put().uri("/migration/prisoners/A1234AA/profile-details-physical-attributes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(SINGLE_CURRENT_RECORD_MIGRATION)
          .exchange()
          .expectStatus().is2xxSuccessful
          .expectBody().jsonPath("$.fieldHistoryInserted[*]").value(not(hasItem(-1)))

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = generateRefDataCode(prisonerHair),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          FACIAL_HAIR,
          HistoryComparison(
            value = generateRefDataCode(prisonerFacialHair),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          FACE,
          HistoryComparison(
            value = generateRefDataCode(prisonerFace),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          BUILD,
          HistoryComparison(
            value = generateRefDataCode(prisonerBuild),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          LEFT_EYE_COLOUR,
          HistoryComparison(
            value = generateRefDataCode(prisonerLeftEyeColour),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          RIGHT_EYE_COLOUR,
          HistoryComparison(
            value = generateRefDataCode(prisonerRightEyeColour),
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          SHOE_SIZE,
          HistoryComparison(
            value = PRISONER_SHOE_SIZE,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldMetadata(
          FieldMetadata(PRISONER_NUMBER, HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, FACIAL_HAIR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, FACE, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, BUILD, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, LEFT_EYE_COLOUR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, RIGHT_EYE_COLOUR, lastModifiedAt = NOW, lastModifiedBy = USER1),
          FieldMetadata(PRISONER_NUMBER, SHOE_SIZE, lastModifiedAt = NOW, lastModifiedBy = USER1),
        )
      }
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    data class RefData(
      val domain: String,
      val code: String,
      val domainDescription: String,
      val codeDescription: String,
      val listSeq: Int = 0,
    ) {
      val id: String
        get() = "${domain}_$code"
    }

    val prisonerHair = RefData(
      domain = "HAIR",
      code = "BROWN",
      domainDescription = "Hair type or colour",
      codeDescription = "Brown",
    )
    val prisonerFacialHair = RefData(
      domain = "FACIAL_HAIR",
      code = "BEARDED",
      domainDescription = "Facial hair type",
      codeDescription = "Full Beard",
    )
    val prisonerFace = RefData(
      domain = "FACE",
      code = "OVAL",
      domainDescription = "Face shape",
      codeDescription = "Oval",
    )
    val prisonerBuild = RefData(
      domain = "BUILD",
      code = "MEDIUM",
      domainDescription = "Build",
      codeDescription = "Medium",
    )
    val prisonerLeftEyeColour = RefData(
      domain = "EYE",
      code = "GREEN",
      domainDescription = "Eye colour",
      codeDescription = "Green",
      listSeq = 1,
    )
    val prisonerRightEyeColour = RefData(
      domain = "EYE",
      code = "BLUE",
      domainDescription = "Eye colour",
      codeDescription = "Blue",
      listSeq = 1,
    )
    const val PRISONER_SHOE_SIZE = "11.5"

    val prisonerHairPrevious = RefData(
      domain = "HAIR",
      code = "BLACK",
      domainDescription = "Hair type or colour",
      codeDescription = "Black",
    )
    val prisonerFacialHairPrevious = RefData(
      domain = "FACIAL_HAIR",
      code = "SIDEBURNS",
      domainDescription = "Facial hair type",
      codeDescription = "Sideburns",
    )
    val prisonerFacePrevious = RefData(
      domain = "FACE",
      code = "ROUND",
      domainDescription = "Face shape",
      codeDescription = "Round",
    )
    val prisonerBuildPrevious = RefData(
      domain = "BUILD",
      code = "HEAVY",
      domainDescription = "Build",
      codeDescription = "Heavy",
    )
    val prisonerLeftEyeColourPrevious = RefData(
      domain = "EYE",
      code = "BROWN",
      domainDescription = "Eye colour",
      codeDescription = "Brown",
      listSeq = 1,
    )
    val prisonerRightEyeColourPrevious = RefData(
      domain = "EYE",
      code = "BROWN",
      domainDescription = "Eye colour",
      codeDescription = "Brown",
      listSeq = 1,
    )
    const val PRISONER_SHOE_SIZE_PREVIOUS = "9"

    val NOW = ZonedDateTime.of(2024, 6, 14, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))
    val THEN = NOW.minusDays(1)
    val PREV_DATE = NOW.minusDays(2)

    val SINGLE_CURRENT_RECORD_MIGRATION =
      """
        [
          {
            "hair": {
                "value": "${prisonerHair.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "facialHair": {
                "value": "${prisonerFacialHair.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "face": {
                "value": "${prisonerFace.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "build": {
                "value": "${prisonerBuild.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "leftEyeColour": {
                "value": "${prisonerLeftEyeColour.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "rightEyeColour": {
                "value": "${prisonerRightEyeColour.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "shoeSize": {
                "value": "$PRISONER_SHOE_SIZE",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "appliesFrom": "$THEN"
          }
        ]
      """.trimIndent()

    val PHYSICAL_ATTRIBUTES_WITH_HISTORY_MIGRATION =
      """
        [
          {
            "hair": {
                "value": "${prisonerHairPrevious.id}",
                "lastModifiedAt": "$THEN",
                "lastModifiedBy": "$USER1"
            },
            "facialHair": {
                "value": "${prisonerFacialHairPrevious.id}",
                "lastModifiedAt": "$THEN",
                "lastModifiedBy": "$USER1"
            },
            "face": {
                "value": "${prisonerFacePrevious.id}",
                "lastModifiedAt": "$THEN",
                "lastModifiedBy": "$USER1"
            },
            "build": {
                "value": "${prisonerBuildPrevious.id}",
                "lastModifiedAt": "$THEN",
                "lastModifiedBy": "$USER1"
            },
            "leftEyeColour": {
                "value": "${prisonerLeftEyeColourPrevious.id}",
                "lastModifiedAt": "$THEN",
                "lastModifiedBy": "$USER1"
            },
            "rightEyeColour": {
                "value": "${prisonerRightEyeColourPrevious.id}",
                "lastModifiedAt": "$THEN",
                "lastModifiedBy": "$USER1"
            },
            "shoeSize": {
                "value": "$PRISONER_SHOE_SIZE_PREVIOUS",
                "lastModifiedAt": "$THEN",
                "lastModifiedBy": "$USER1"
            },
            "appliesFrom": "$PREV_DATE",
            "appliesTo": "$THEN"
          },
          {
            "hair": {
                "value": "${prisonerHair.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "facialHair": {
                "value": "${prisonerFacialHair.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "face": {
                "value": "${prisonerFace.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "build": {
                "value": "${prisonerBuild.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "leftEyeColour": {
                "value": "${prisonerLeftEyeColour.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "rightEyeColour": {
                "value": "${prisonerRightEyeColour.id}",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "shoeSize": {
                "value": "$PRISONER_SHOE_SIZE",
                "lastModifiedAt": "$NOW",
                "lastModifiedBy": "$USER1"
            },
            "appliesFrom": "$THEN"
          }
        ]
      """.trimIndent()

    val VALID_REQUEST_BODY = SINGLE_CURRENT_RECORD_MIGRATION

    val DOMAIN = ReferenceDataDomain("DOMAIN", "Domain", 0, ZonedDateTime.now(), "testUser")
    val REF_DATA_CODE =
      ReferenceDataCode(
        "${DOMAIN.code}_ACTIVE",
        "ACTIVE",
        DOMAIN,
        "Active domain",
        0,
        ZonedDateTime.now(),
        "testUser",
      )

    fun generateRefDataCode(
      refData: RefData?,
    ): ReferenceDataCode {
      if (refData == null) return null as ReferenceDataCode

      val refDataDomain =
        ReferenceDataDomain(refData.domain, refData.domainDescription, 0, ZonedDateTime.now(), "testUser")
      return ReferenceDataCode(
        "${refData.domain}_${refData.code}",
        refData.code,
        refDataDomain,
        refData.codeDescription,
        refData.listSeq,
        ZonedDateTime.now(),
        "testUser",
      )
    }
  }
}
