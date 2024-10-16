package uk.gov.justice.digital.hmpps.prisonperson.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.MigrationValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.ProfileDetailsPhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ProfileDetailsPhysicalAttributesMigrationResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectDomainEventRaised
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.PrisonPersonUpdatedEvent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ProfileDetailsPhysicalAttributesMigrationServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var physicalAttributesService: PhysicalAttributesService

  @Mock
  lateinit var telemetryClient: TelemetryClient

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: ProfileDetailsPhysicalAttributesMigrationService

  private val savedPhysicalAttributes = argumentCaptor<PhysicalAttributes>()

  @Nested
  inner class MigrateProfileDetailsPhysicalAttributes {

    @BeforeEach
    fun beforeEach() {
      lenient().whenever(referenceDataCodeRepository.findById(any())).thenAnswer { invocation ->
        val id = invocation.arguments[0] as String
        splitId(id)?.let { (domain, code) ->
          Optional.ofNullable(generateRefDataCode(code, domain))
        }
      }
      whenever(physicalAttributesRepository.save(savedPhysicalAttributes.capture())).thenAnswer { savedPhysicalAttributes.firstValue }
      whenever(physicalAttributesService.newPhysicalAttributesFor(PRISONER_NUMBER)).thenReturn(
        PhysicalAttributes(
          PRISONER_NUMBER,
        ),
      )
    }

    @Test
    fun `migrates a single version of physical attributes`() {
      assertThat(
        underTest.migrate(
          PRISONER_NUMBER,
          sortedSetOf(PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST),
        ),
      )
        .isInstanceOf(ProfileDetailsPhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(hair?.code).isEqualTo(PRISONER_HAIR.value)
        assertThat(facialHair?.code).isEqualTo(PRISONER_FACIAL_HAIR.value)
        assertThat(face?.code).isEqualTo(PRISONER_FACE.value)
        assertThat(build?.code).isEqualTo(PRISONER_BUILD.value)
        assertThat(leftEyeColour?.code).isEqualTo(PRISONER_LEFT_EYE_COLOUR.value)
        assertThat(rightEyeColour?.code).isEqualTo(PRISONER_RIGHT_EYE_COLOUR.value)
        assertThat(shoeSize).isEqualTo(PRISONER_SHOE_SIZE.value)

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = generateRefDataCode(PRISONER_HAIR.value, "HAIR"),
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
            value = generateRefDataCode(PRISONER_FACIAL_HAIR.value, "FACIAL_HAIR"),
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
            value = generateRefDataCode(PRISONER_FACE.value, "FACE"),
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
            value = generateRefDataCode(PRISONER_BUILD.value, "BUILD"),
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
            value = generateRefDataCode(PRISONER_LEFT_EYE_COLOUR.value, "EYE"),
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
            value = generateRefDataCode(PRISONER_RIGHT_EYE_COLOUR.value, "EYE"),
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
            value = PRISONER_SHOE_SIZE.value,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `migrates a history of profile details physical attributes`() {
      val record1 = PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST
      val record2 = generatePrevious(record1, USER2, 0)
      val record3 = generatePrevious(record2, USER3, 1)

      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(record1, record2, record3)))
        .isInstanceOf(ProfileDetailsPhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(hair?.code).isEqualTo(PRISONER_HAIR.value)
        assertThat(facialHair?.code).isEqualTo(PRISONER_FACIAL_HAIR.value)
        assertThat(face?.code).isEqualTo(PRISONER_FACE.value)
        assertThat(build?.code).isEqualTo(PRISONER_BUILD.value)
        assertThat(leftEyeColour?.code).isEqualTo(PRISONER_LEFT_EYE_COLOUR.value)
        assertThat(rightEyeColour?.code).isEqualTo(PRISONER_RIGHT_EYE_COLOUR.value)
        assertThat(shoeSize).isEqualTo(PRISONER_SHOE_SIZE.value)

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = generateRefDataCode(RECORDS["HAIR"]!![1], "HAIR"),
            createdAt = NOW.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(RECORDS["HAIR"]!![0], "HAIR"),
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(PRISONER_HAIR.value, "HAIR"),
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
            value = generateRefDataCode(RECORDS["FACIAL_HAIR"]!![1], "FACIAL_HAIR"),
            createdAt = NOW.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(RECORDS["FACIAL_HAIR"]!![0], "FACIAL_HAIR"),
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(PRISONER_FACIAL_HAIR.value, "FACIAL_HAIR"),
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
            value = generateRefDataCode(RECORDS["FACE"]!![1], "FACE"),
            createdAt = NOW.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(RECORDS["FACE"]!![0], "FACE"),
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(PRISONER_FACE.value, "FACE"),
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
            value = generateRefDataCode(RECORDS["BUILD"]!![1], "BUILD"),
            createdAt = NOW.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(RECORDS["BUILD"]!![0], "BUILD"),
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(PRISONER_BUILD.value, "BUILD"),
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
            value = generateRefDataCode(RECORDS["LEFT_EYE_COLOUR"]!![1], "EYE"),
            createdAt = NOW.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(RECORDS["LEFT_EYE_COLOUR"]!![0], "EYE"),
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(PRISONER_LEFT_EYE_COLOUR.value, "EYE"),
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
            value = generateRefDataCode(RECORDS["RIGHT_EYE_COLOUR"]!![1], "EYE"),
            createdAt = NOW.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(RECORDS["RIGHT_EYE_COLOUR"]!![0], "EYE"),
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = generateRefDataCode(PRISONER_RIGHT_EYE_COLOUR.value, "EYE"),
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
            value = RECORDS["SHOE_SIZE"]!![1],
            createdAt = NOW.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = RECORDS["SHOE_SIZE"]!![0],
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = PRISONER_SHOE_SIZE.value,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `ignores history record if profile details physical attributes have not changed`() {
      val record1 = PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST
      val record2 = record1.copy(appliesFrom = record1.appliesFrom.plusDays(1))

      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(record1, record2)))
        .isInstanceOf(ProfileDetailsPhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(hair?.code).isEqualTo(PRISONER_HAIR.value)
        assertThat(facialHair?.code).isEqualTo(PRISONER_FACIAL_HAIR.value)
        assertThat(face?.code).isEqualTo(PRISONER_FACE.value)
        assertThat(build?.code).isEqualTo(PRISONER_BUILD.value)
        assertThat(leftEyeColour?.code).isEqualTo(PRISONER_LEFT_EYE_COLOUR.value)
        assertThat(rightEyeColour?.code).isEqualTo(PRISONER_RIGHT_EYE_COLOUR.value)
        assertThat(shoeSize).isEqualTo(PRISONER_SHOE_SIZE.value)

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = generateRefDataCode(PRISONER_HAIR.value, "HAIR"),
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
            value = generateRefDataCode(PRISONER_FACIAL_HAIR.value, "FACIAL_HAIR"),
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
            value = generateRefDataCode(PRISONER_FACE.value, "FACE"),
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
            value = generateRefDataCode(PRISONER_BUILD.value, "BUILD"),
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
            value = generateRefDataCode(PRISONER_LEFT_EYE_COLOUR.value, "EYE"),
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
            value = generateRefDataCode(PRISONER_RIGHT_EYE_COLOUR.value, "EYE"),
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
            value = PRISONER_SHOE_SIZE.value,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `migrates profile details physical attributes with nulls`() {
      assertThat(
        underTest.migrate(
          PRISONER_NUMBER,
          sortedSetOf(
            PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST.copy(
              hair = MigrationValueWithMetadata(null, NOW, USER1),
              facialHair = MigrationValueWithMetadata(null, NOW, USER1),
              face = MigrationValueWithMetadata(null, NOW, USER1),
              build = MigrationValueWithMetadata(null, NOW, USER1),
              leftEyeColour = MigrationValueWithMetadata(null, NOW, USER1),
              rightEyeColour = MigrationValueWithMetadata(null, NOW, USER1),
              shoeSize = MigrationValueWithMetadata(null, NOW, USER1),
            ),
          ),
        ),
      )
        .isInstanceOf(ProfileDetailsPhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(hair?.id).isEqualTo(null)
        assertThat(facialHair?.id).isEqualTo(null)
        assertThat(face?.id).isEqualTo(null)
        assertThat(build?.id).isEqualTo(null)
        assertThat(leftEyeColour?.id).isEqualTo(null)
        assertThat(rightEyeColour?.id).isEqualTo(null)
        assertThat(shoeSize).isEqualTo(null)

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = null,
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
            value = null,
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
            value = null,
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
            value = null,
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
            value = null,
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
            value = null,
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
            value = null,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )
        expectDomainEventRaised(PRISONER_NUMBER, PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }
  }

  @Test
  fun `handles empty migration`() {
    assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf())).isEqualTo(
      ProfileDetailsPhysicalAttributesMigrationResponse(),
    )

    verifyNoInteractions(physicalAttributesRepository)
  }

  private fun generatePrevious(
    migration: ProfileDetailsPhysicalAttributesMigrationRequest,
    username: String,
    idx: Int,
  ): ProfileDetailsPhysicalAttributesMigrationRequest = migration.copy(
    hair = MigrationValueWithMetadata(
      RECORDS["HAIR"]!![idx],
      migration.hair!!.lastModifiedAt.minusDays(1),
      username,
    ),
    facialHair = MigrationValueWithMetadata(
      RECORDS["FACIAL_HAIR"]!![idx],
      migration.facialHair!!.lastModifiedAt.minusDays(1),
      username,
    ),
    face = MigrationValueWithMetadata(
      RECORDS["FACE"]!![idx],
      migration.face!!.lastModifiedAt.minusDays(1),
      username,
    ),
    build = MigrationValueWithMetadata(
      RECORDS["BUILD"]!![idx],
      migration.build!!.lastModifiedAt.minusDays(1),
      username,
    ),
    leftEyeColour = MigrationValueWithMetadata(
      RECORDS["LEFT_EYE_COLOUR"]!![idx],
      migration.leftEyeColour!!.lastModifiedAt.minusDays(1),
      username,
    ),
    rightEyeColour = MigrationValueWithMetadata(
      RECORDS["RIGHT_EYE_COLOUR"]!![idx],
      migration.rightEyeColour!!.lastModifiedAt.minusDays(1),
      username,
    ),
    shoeSize = MigrationValueWithMetadata(
      RECORDS["SHOE_SIZE"]!![idx],
      migration.shoeSize!!.lastModifiedAt.minusDays(1),
      username,
    ),
    appliesFrom = migration.appliesFrom.minusDays(1),
    appliesTo = migration.appliesFrom,
  )

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"
    const val USER3 = "USER3"

    val NOW: ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/London"))
    val THEN: ZonedDateTime = NOW.minusDays(1)

    val PRISONER_HAIR = MigrationValueWithMetadata("GREY", NOW, USER1)
    val PRISONER_FACIAL_HAIR = MigrationValueWithMetadata("BEARDED", NOW, USER1)
    val PRISONER_FACE = MigrationValueWithMetadata("OVAL", NOW, USER1)
    val PRISONER_BUILD = MigrationValueWithMetadata("MEDIUM", NOW, USER1)
    val PRISONER_LEFT_EYE_COLOUR = MigrationValueWithMetadata("GREEN", NOW, USER1)
    val PRISONER_RIGHT_EYE_COLOUR = MigrationValueWithMetadata("BLUE", NOW, USER1)
    val PRISONER_SHOE_SIZE = MigrationValueWithMetadata("11.5", NOW, USER1)

    val RECORDS = mapOf(
      "HAIR" to listOf("BLACK", "BLONDE"),
      "FACIAL_HAIR" to listOf("MOUSTACHE", "SIDEBURNS"),
      "FACE" to listOf("ANGULAR", "SQUARE"),
      "BUILD" to listOf("HEAVY", "SMALL"),
      "LEFT_EYE_COLOUR" to listOf("BROWN", "HAZEL"),
      "RIGHT_EYE_COLOUR" to listOf("GREY", "CLOUDED"),
      "SHOE_SIZE" to listOf("10", "9"),
    )

    val PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST = ProfileDetailsPhysicalAttributesMigrationRequest(
      hair = PRISONER_HAIR,
      facialHair = PRISONER_FACIAL_HAIR,
      face = PRISONER_FACE,
      build = PRISONER_BUILD,
      leftEyeColour = PRISONER_LEFT_EYE_COLOUR,
      rightEyeColour = PRISONER_RIGHT_EYE_COLOUR,
      shoeSize = PRISONER_SHOE_SIZE,
      appliesFrom = THEN,
      latestBooking = true,
    )

    val domains = setOf("HAIR", "FACIAL_HAIR", "FACE", "BUILD", "EYE")

    fun splitId(id: String?): Pair<String, String>? {
      if (id.isNullOrEmpty()) return null

      for (domain in domains) {
        if (id.startsWith("${domain}_")) {
          val code = id.removePrefix("${domain}_")
          return domain to code
        }
      }

      return null
    }

    fun generateRefDataCode(code: String?, domain: String): ReferenceDataCode? {
      if (code == null) return null

      val rdd = ReferenceDataDomain(domain, "Domain", 0, ZonedDateTime.now(), "testUser")
      return ReferenceDataCode("${domain}_$code", code, rdd, "Ref data code", 0, ZonedDateTime.now(), "testUser")
    }
  }
}
