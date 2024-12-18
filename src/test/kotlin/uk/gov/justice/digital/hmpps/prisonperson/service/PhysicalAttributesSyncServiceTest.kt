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
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesSyncDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesSyncResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_SYNCED
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectDomainEventRaised
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.PrisonPersonUpdatedEvent
import java.time.Clock
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PhysicalAttributesSyncServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Mock
  lateinit var fieldHistoryRepository: FieldHistoryRepository

  @Mock
  lateinit var physicalAttributesService: PhysicalAttributesService

  @Mock
  lateinit var telemetryClient: TelemetryClient

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: PhysicalAttributesSyncService

  private val savedPhysicalAttributes = argumentCaptor<PhysicalAttributes>()
  private val savedFieldHistory = argumentCaptor<FieldHistory>()

  @Nested
  inner class SyncPhysicalAttributes {

    @BeforeEach
    fun beforeEach() {
      whenever(physicalAttributesRepository.save(savedPhysicalAttributes.capture())).thenAnswer { savedPhysicalAttributes.firstValue }
      whenever(fieldHistoryRepository.save(savedFieldHistory.capture())).thenAnswer { savedFieldHistory.firstValue }
    }

    @Test
    fun `creates new physical attributes`() {
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

      assertThat(underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST))
        .isInstanceOf(PhysicalAttributesSyncResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = PRISONER_HEIGHT,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = PRISONER_WEIGHT,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNCED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `does not create new physical attributes if prisoner doesn't exist in prisoner search`() {
      whenever(physicalAttributesService.ensurePhysicalAttributesPersistedFor(PRISONER_NUMBER)).thenThrow(
        IllegalArgumentException("Prisoner number '$PRISONER_NUMBER' not found"),
      )
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy { underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("Prisoner number '$PRISONER_NUMBER' not found")

      verify(physicalAttributesRepository, never()).save(any())
      verifyNoInteractions(telemetryClient)
    }

    @Test
    fun `updates physical attributes`() {
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            height = PREVIOUS_PRISONER_HEIGHT,
            weight = PREVIOUS_PRISONER_WEIGHT,
          ).also { it.updateFieldHistory(lastModifiedAt = NOW.minusDays(1), lastModifiedBy = USER1) },
        ),
      )

      assertThat(underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST.copy(createdBy = USER2)))
        .isInstanceOf(PhysicalAttributesSyncResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)

        expectFieldHistory(
          HEIGHT,
          // Initial history entry:
          HistoryComparison(
            value = PREVIOUS_PRISONER_HEIGHT,
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = NOW,
          ),
          // New history entry:
          HistoryComparison(
            value = PRISONER_HEIGHT,
            createdAt = NOW,
            createdBy = USER2,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          // Initial history entry:
          HistoryComparison(
            value = PREVIOUS_PRISONER_WEIGHT,
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            appliesFrom = NOW.minusDays(1),
            appliesTo = NOW,
          ),
          // New history entry:
          HistoryComparison(
            value = PRISONER_WEIGHT,
            createdAt = NOW,
            createdBy = USER2,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNCED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `inserts physical attributes history (for updates to old NOMIS bookings)`() {
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            height = PRISONER_HEIGHT,
            weight = PRISONER_WEIGHT,
          ).also { it.updateFieldHistory(lastModifiedAt = NOW.minusDays(1), lastModifiedBy = USER1) },
        ),
      )

      assertThat(
        underTest.sync(
          PRISONER_NUMBER,
          PhysicalAttributesSyncRequest(
            height = PREVIOUS_PRISONER_HEIGHT,
            weight = PREVIOUS_PRISONER_WEIGHT,
            appliesFrom = NOW.minusDays(10),
            appliesTo = NOW.minusDays(5),
            createdAt = NOW,
            createdBy = USER2,
            latestBooking = false,
          ),
        ),
      )
        .isInstanceOf(PhysicalAttributesSyncResponse::class.java)

      // Main physical attributes record remains unchanged:
      assertThat(savedPhysicalAttributes.firstValue.height).isEqualTo(PRISONER_HEIGHT)
      assertThat(savedPhysicalAttributes.firstValue.weight).isEqualTo(PRISONER_WEIGHT)

      expectFieldHistory(
        HEIGHT,
        savedFieldHistory.allValues,
        HistoryComparison(
          value = PREVIOUS_PRISONER_HEIGHT,
          createdAt = NOW,
          createdBy = USER2,
          appliesFrom = NOW.minusDays(10),
          appliesTo = NOW.minusDays(5),
          source = NOMIS,
        ),
      )

      expectFieldHistory(
        WEIGHT,
        savedFieldHistory.allValues,
        HistoryComparison(
          value = PREVIOUS_PRISONER_WEIGHT,
          createdAt = NOW,
          createdBy = USER2,
          appliesFrom = NOW.minusDays(10),
          appliesTo = NOW.minusDays(5),
          source = NOMIS,
        ),
      )

      savedPhysicalAttributes.firstValue.expectDomainEventRaised(
        PRISONER_NUMBER,
        PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL,
      ) {
        assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
      }
    }

    @Test
    fun `accepts physical attributes with null height and weight`() {
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

      assertThat(underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST.copy(height = null, weight = null)))
        .isInstanceOf(PhysicalAttributesSyncResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(null)
        assertThat(weight).isEqualTo(null)

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = null,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = null,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNCED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }
  }

  @Nested
  inner class GetPhysicalAttributes {
    @Test
    fun `physical attributes not found`() {
      whenever(physicalAttributesRepository.findByIdForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(underTest.getPhysicalAttributes(PRISONER_NUMBER)).isNull()
    }

    @Test
    fun `physical attributes retrieved`() {
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            height = PRISONER_HEIGHT,
            weight = PRISONER_WEIGHT,
            hair = PRISONER_HAIR,
            facialHair = PRISONER_FACIAL_HAIR,
            face = PRISONER_FACE,
            build = PRISONER_BUILD,
            leftEyeColour = PRISONER_LEFT_EYE_COLOUR,
            rightEyeColour = PRISONER_RIGHT_EYE_COLOUR,
            shoeSize = PRISONER_SHOE_SIZE,
          ),
        ),
      )

      assertThat(underTest.getPhysicalAttributes(PRISONER_NUMBER))
        .isEqualTo(
          PhysicalAttributesSyncDto(
            height = PRISONER_HEIGHT,
            weight = PRISONER_WEIGHT,
            hair = PRISONER_HAIR!!.code,
            facialHair = PRISONER_FACIAL_HAIR!!.code,
            face = PRISONER_FACE!!.code,
            build = PRISONER_BUILD!!.code,
            leftEyeColour = PRISONER_LEFT_EYE_COLOUR!!.code,
            rightEyeColour = PRISONER_RIGHT_EYE_COLOUR!!.code,
            shoeSize = PRISONER_SHOE_SIZE,
          ),
        )
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70

    const val PREVIOUS_PRISONER_HEIGHT = 179
    const val PREVIOUS_PRISONER_WEIGHT = 69
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

    val PHYSICAL_ATTRIBUTES_SYNC_REQUEST = PhysicalAttributesSyncRequest(
      PRISONER_HEIGHT,
      PRISONER_WEIGHT,
      appliesFrom = NOW,
      appliesTo = null,
      createdAt = NOW,
      createdBy = USER1,
    )

    fun generateRefDataCode(code: String?, domain: String): ReferenceDataCode? {
      if (code == null) return null

      val rdd = ReferenceDataDomain(domain, "Domain", 0, ZonedDateTime.now(), "testUser")
      return ReferenceDataCode("${domain}_$code", code, rdd, "Ref data code", 0, ZonedDateTime.now(), "testUser")
    }
  }
}
