package uk.gov.justice.digital.hmpps.prisonperson.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.ProfileDetailsPhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.SyncValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ProfileDetailsPhysicalAttributesSyncResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectDomainEventRaised
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.PrisonPersonUpdatedEvent
import java.time.Clock
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class ProfileDetailsPhysicalAttributesSyncServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Mock
  lateinit var fieldHistoryRepository: FieldHistoryRepository

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var physicalAttributesService: PhysicalAttributesService

  @Mock
  lateinit var telemetryClient: TelemetryClient

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: ProfileDetailsPhysicalAttributesSyncService

  private val savedPhysicalAttributes = argumentCaptor<PhysicalAttributes>()
  private val savedFieldHistory = argumentCaptor<FieldHistory>()

  @Nested
  inner class SyncProfileDetailsPhysicalAttributes {

    @BeforeEach
    fun beforeEach() {
      whenever(physicalAttributesRepository.save(savedPhysicalAttributes.capture())).thenAnswer { savedPhysicalAttributes.lastValue }
      whenever(referenceDataCodeRepository.findById("HAIR_GREY")).thenAnswer { Optional.of(PRISONER_HAIR) }
      whenever(referenceDataCodeRepository.findById("HAIR_BLACK")).thenAnswer { Optional.of(PREVIOUS_PRISONER_HAIR) }
      whenever(fieldHistoryRepository.save(savedFieldHistory.capture())).thenAnswer { savedFieldHistory.lastValue }
    }

    @Test
    fun `creates new profile details physical attributes`() {
      whenever(physicalAttributesService.newPhysicalAttributesFor(PRISONER_NUMBER)).thenReturn(
        PhysicalAttributes(
          PRISONER_NUMBER,
        ),
      )
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            PRISONER_NUMBER,
          ),
        ),
      )

      assertThat(underTest.sync(PRISONER_NUMBER, profileDetailsPhysicalAttributesSyncRequestFactory(HAIR)))
        .isInstanceOf(ProfileDetailsPhysicalAttributesSyncResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(hair).isEqualTo(PRISONER_HAIR)

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = PRISONER_HAIR,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `does not create new profile details physical attributes if prisoner doesn't exist in prisoner search`() {
      whenever(physicalAttributesService.ensurePhysicalAttributesPersistedFor(PRISONER_NUMBER)).thenThrow(
        IllegalArgumentException("Prisoner number '$PRISONER_NUMBER' not found"),
      )
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy {
        underTest.sync(
          PRISONER_NUMBER,
          profileDetailsPhysicalAttributesSyncRequestFactory(HAIR),
        )
      }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("Prisoner number '$PRISONER_NUMBER' not found")

      verify(physicalAttributesRepository, never()).save(any())
      verifyNoInteractions(telemetryClient)
    }

    @Test
    fun `updates profile details physical attributes`() {
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            hair = PREVIOUS_PRISONER_HAIR,
          ).also { it.updateFieldHistory(lastModifiedAt = NOW.minusDays(1), lastModifiedBy = USER1) },
        ),
      )

      assertThat(
        underTest.sync(
          PRISONER_NUMBER,
          profileDetailsPhysicalAttributesSyncRequestFactory(HAIR).copy(
            hair = SyncValueWithMetadata(
              PRISONER_HAIR.code,
              NOW,
              USER2,
            ),
          ),
        ),
      )
        .isInstanceOf(ProfileDetailsPhysicalAttributesSyncResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(hair).isEqualTo(PRISONER_HAIR)

        expectFieldHistory(
          HAIR,
          // Initial history entry:
          HistoryComparison(
            value = PREVIOUS_PRISONER_HAIR,
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = NOW,
          ),
          // New history entry:
          HistoryComparison(
            value = PRISONER_HAIR,
            createdAt = NOW,
            createdBy = USER2,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `inserts profile details physical attributes history (for updates to old NOMIS bookings)`() {
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            hair = PRISONER_HAIR,
          ).also { it.updateFieldHistory(lastModifiedAt = NOW.minusDays(1), lastModifiedBy = USER1) },
        ),
      )

      assertThat(
        underTest.sync(
          PRISONER_NUMBER,
          ProfileDetailsPhysicalAttributesSyncRequest(
            hair = SyncValueWithMetadata(PREVIOUS_PRISONER_HAIR.code, NOW, USER2),
            appliesFrom = NOW.minusDays(10),
            appliesTo = NOW.minusDays(5),
            latestBooking = false,
          ),
        ),
      )
        .isInstanceOf(ProfileDetailsPhysicalAttributesSyncResponse::class.java)

      // Main physical attributes record remains unchanged:
      assertThat(savedPhysicalAttributes.firstValue.hair).isEqualTo(PRISONER_HAIR)

      expectFieldHistory(
        HAIR,
        savedFieldHistory.allValues,
        HistoryComparison(
          value = PREVIOUS_PRISONER_HAIR,
          createdAt = NOW,
          createdBy = USER2,
          appliesFrom = NOW.minusDays(10),
          appliesTo = NOW.minusDays(5),
          source = NOMIS,
        ),
      )

      savedPhysicalAttributes.firstValue.expectDomainEventRaised(
        PRISONER_NUMBER,
        PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL,
      ) {
        assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
      }
    }

    @Test
    fun `accepts profie details physical attributes with null hair`() {
      whenever(physicalAttributesService.newPhysicalAttributesFor(PRISONER_NUMBER)).thenReturn(
        PhysicalAttributes(
          PRISONER_NUMBER,
        ),
      )
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            PRISONER_NUMBER,
          ),
        ),
      )

      assertThat(
        underTest.sync(
          PRISONER_NUMBER,
          profileDetailsPhysicalAttributesSyncRequestFactory(HAIR).copy(
            hair = SyncValueWithMetadata(
              null,
              NOW,
              USER1,
            ),
          ),
        ),
      )
        .isInstanceOf(ProfileDetailsPhysicalAttributesSyncResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(hair).isEqualTo(null)

        expectFieldHistory(
          HAIR,
          HistoryComparison(
            value = null,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW: ZonedDateTime = ZonedDateTime.now()

    val PRISONER_HAIR = generateRefDataCode("GREY", "HAIR")
    val PRISONER_FACIAL_HAIR = generateRefDataCode("BEARDED", "FACIAL_HAIR")
    val PRISONER_FACE = generateRefDataCode("OVAL", "FACE")
    val PRISONER_BUILD = generateRefDataCode("MEDIUM", "BUILD")
    val PRISONER_LEFT_EYE_COLOUR = generateRefDataCode("GREEN", "EYE")
    val PRISONER_RIGHT_EYE_COLOUR = generateRefDataCode("BLUE", "EYE")
    val PRISONER_SHOE_SIZE = "11.5"

    val PREVIOUS_PRISONER_HAIR = generateRefDataCode("BLACK", "HAIR")

    fun profileDetailsPhysicalAttributesSyncRequestFactory(attribute: PrisonPersonField) =
      when (attribute) {
        HAIR -> ProfileDetailsPhysicalAttributesSyncRequest(
          hair = SyncValueWithMetadata(PRISONER_HAIR.code, NOW, USER1),
          appliesFrom = NOW,
          appliesTo = null,
        )

        FACIAL_HAIR -> ProfileDetailsPhysicalAttributesSyncRequest(
          facialHair = SyncValueWithMetadata(PRISONER_FACIAL_HAIR.code, NOW, USER1),
          appliesFrom = NOW,
          appliesTo = null,
        )

        FACE -> ProfileDetailsPhysicalAttributesSyncRequest(
          face = SyncValueWithMetadata(PRISONER_FACE.code, NOW, USER1),
          appliesFrom = NOW,
          appliesTo = null,
        )

        BUILD -> ProfileDetailsPhysicalAttributesSyncRequest(
          build = SyncValueWithMetadata(PRISONER_BUILD.code, NOW, USER1),
          appliesFrom = NOW,
          appliesTo = null,
        )

        LEFT_EYE_COLOUR -> ProfileDetailsPhysicalAttributesSyncRequest(
          leftEyeColour = SyncValueWithMetadata(PRISONER_LEFT_EYE_COLOUR.code, NOW, USER1),
          appliesFrom = NOW,
          appliesTo = null,
        )

        RIGHT_EYE_COLOUR -> ProfileDetailsPhysicalAttributesSyncRequest(
          rightEyeColour = SyncValueWithMetadata(PRISONER_RIGHT_EYE_COLOUR.code, NOW, USER1),
          appliesFrom = NOW,
          appliesTo = null,
        )

        SHOE_SIZE -> ProfileDetailsPhysicalAttributesSyncRequest(
          shoeSize = SyncValueWithMetadata(PRISONER_SHOE_SIZE, NOW, USER1),
          appliesFrom = NOW,
          appliesTo = null,
        )

        else -> throw IllegalArgumentException("Invalid attribute: $attribute")
      }

    fun generateRefDataCode(code: String, domain: String): ReferenceDataCode {
      val rdd = ReferenceDataDomain(domain, "Domain", 0, ZonedDateTime.now(), "testUser")
      return ReferenceDataCode("${domain}_$code", code, rdd, "Ref data code", 0, ZonedDateTime.now(), "testUser")
    }
  }
}
