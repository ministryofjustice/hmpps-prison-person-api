package uk.gov.justice.digital.hmpps.prisonperson.service

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
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesHistoryDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributesHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PhysicalAttributesSyncServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Mock
  lateinit var physicalAttributesHistoryRepository: PhysicalAttributesHistoryRepository

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: PhysicalAttributesSyncService

  private val savedPhysicalAttributes = argumentCaptor<PhysicalAttributes>()
  private val savedPhysicalAttributesHistory = argumentCaptor<PhysicalAttributesHistory>()

  @Nested
  inner class SyncPhysicalAttributes {

    @BeforeEach
    fun beforeEach() {
      whenever(physicalAttributesRepository.save(savedPhysicalAttributes.capture())).thenAnswer { savedPhysicalAttributes.firstValue }
      whenever(physicalAttributesHistoryRepository.save(savedPhysicalAttributesHistory.capture())).thenAnswer { savedPhysicalAttributesHistory.firstValue }
    }

    @Test
    fun `creates new physical attributes`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(PRISONER_SEARCH_RESPONSE)
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST))
        .isEqualTo(
          PhysicalAttributesHistoryDto(
            physicalAttributesHistoryId = HISTORY_ID,
            height = PRISONER_HEIGHT,
            weight = PRISONER_WEIGHT,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
          ),
        )

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)
        assertThat(createdAt).isEqualTo(NOW)
        assertThat(createdBy).isEqualTo(USER1)
        assertThat(lastModifiedAt).isEqualTo(NOW)
        assertThat(lastModifiedBy).isEqualTo(USER1)
        assertThat(migratedAt).isNull()

        assertThat(history).hasSize(1)
        with(history.first()) {
          assertThat(height).isEqualTo(PRISONER_HEIGHT)
          assertThat(weight).isEqualTo(PRISONER_WEIGHT)
          assertThat(createdAt).isEqualTo(NOW)
          assertThat(createdBy).isEqualTo(USER1)
          assertThat(appliesFrom).isEqualTo(NOW)
          assertThat(appliesTo).isNull()
          assertThat(migratedAt).isNull()
        }
      }
    }

    @Test
    fun `does not create new physical attributes if prisoner doesn't exist in prisoner search`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy { underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("Prisoner number '$PRISONER_NUMBER' not found")

      verify(physicalAttributesRepository, never()).save(any())
    }

    @Test
    fun `updates physical attributes`() {
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            height = PREVIOUS_PRISONER_HEIGHT,
            weight = PREVIOUS_PRISONER_WEIGHT,
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            lastModifiedAt = NOW.minusDays(1),
            lastModifiedBy = USER1,
          ).also { it.addToHistory() },
        ),
      )

      assertThat(underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST.copy(createdBy = USER2)))
        .isEqualTo(
          PhysicalAttributesHistoryDto(
            physicalAttributesHistoryId = HISTORY_ID,
            height = PRISONER_HEIGHT,
            weight = PRISONER_WEIGHT,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER2,
          ),
        )

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)
        assertThat(createdAt).isEqualTo(NOW.minusDays(1))
        assertThat(createdBy).isEqualTo(USER1)
        assertThat(lastModifiedAt).isEqualTo(NOW)
        assertThat(lastModifiedBy).isEqualTo(USER2)
        assertThat(migratedAt).isNull()

        assertThat(history).hasSize(2)
        // Initial history entry:
        with(getHistoryAsList()[0]) {
          assertThat(height).isEqualTo(PREVIOUS_PRISONER_HEIGHT)
          assertThat(weight).isEqualTo(PREVIOUS_PRISONER_WEIGHT)
          assertThat(appliesFrom).isEqualTo(NOW.minusDays(1))
          assertThat(appliesTo).isEqualTo(NOW)
          assertThat(migratedAt).isNull()
          assertThat(createdAt).isEqualTo(NOW.minusDays(1))
          assertThat(createdBy).isEqualTo(USER1)
        }
        // New history entry:
        with(getHistoryAsList()[1]) {
          assertThat(height).isEqualTo(PRISONER_HEIGHT)
          assertThat(weight).isEqualTo(PRISONER_WEIGHT)
          assertThat(appliesFrom).isEqualTo(NOW)
          assertThat(appliesTo).isNull()
          assertThat(migratedAt).isNull()
          assertThat(createdAt).isEqualTo(NOW)
          assertThat(createdBy).isEqualTo(USER2)
        }
      }
    }

    @Test
    fun `inserts physical attributes history (for updates to old NOMIS bookings)`() {
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            height = PRISONER_HEIGHT,
            weight = PRISONER_WEIGHT,
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            lastModifiedAt = NOW.minusDays(1),
            lastModifiedBy = USER1,
          ).also { it.addToHistory() },
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
          ),
        ),
      )
        .isEqualTo(
          PhysicalAttributesHistoryDto(
            physicalAttributesHistoryId = HISTORY_ID,
            height = PREVIOUS_PRISONER_HEIGHT,
            weight = PREVIOUS_PRISONER_WEIGHT,
            appliesFrom = NOW.minusDays(10),
            appliesTo = NOW.minusDays(5),
            createdAt = NOW,
            createdBy = USER2,
          ),
        )

      with(savedPhysicalAttributesHistory.firstValue) {
        assertThat(height).isEqualTo(PREVIOUS_PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PREVIOUS_PRISONER_WEIGHT)
        assertThat(appliesFrom).isEqualTo(NOW.minusDays(10))
        assertThat(appliesTo).isEqualTo(NOW.minusDays(5))
        assertThat(createdAt).isEqualTo(NOW)
        assertThat(createdBy).isEqualTo(USER2)
        assertThat(migratedAt).isNull()

        with(physicalAttributes) {
          assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
          assertThat(height).isEqualTo(PRISONER_HEIGHT)
          assertThat(weight).isEqualTo(PRISONER_WEIGHT)
          assertThat(createdAt).isEqualTo(NOW.minusDays(1))
          assertThat(createdBy).isEqualTo(USER1)
          assertThat(lastModifiedAt).isEqualTo(NOW.minusDays(1))
          assertThat(lastModifiedBy).isEqualTo(USER1)
          assertThat(migratedAt).isNull()
        }
      }
    }

    @Test
    fun `accepts physical attributes with null height and weight`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(PRISONER_SEARCH_RESPONSE)
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST.copy(height = null, weight = null)))
        .isEqualTo(
          PhysicalAttributesHistoryDto(
            physicalAttributesHistoryId = HISTORY_ID,
            height = null,
            weight = null,
            appliesFrom = NOW,
            appliesTo = null,
            createdAt = NOW,
            createdBy = USER1,
          ),
        )

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(null)
        assertThat(weight).isEqualTo(null)
        assertThat(createdAt).isEqualTo(NOW)
        assertThat(createdBy).isEqualTo(USER1)
        assertThat(lastModifiedAt).isEqualTo(NOW)
        assertThat(lastModifiedBy).isEqualTo(USER1)
        assertThat(migratedAt).isNull()

        assertThat(history).hasSize(1)
        with(history.first()) {
          assertThat(height).isEqualTo(null)
          assertThat(weight).isEqualTo(null)
          assertThat(createdAt).isEqualTo(NOW)
          assertThat(createdBy).isEqualTo(USER1)
          assertThat(appliesFrom).isEqualTo(NOW)
          assertThat(appliesTo).isNull()
          assertThat(migratedAt).isNull()
        }
      }
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
    const val HISTORY_ID = -1L

    val NOW: ZonedDateTime = ZonedDateTime.now()

    val PRISONER_SEARCH_RESPONSE =
      PrisonerDto(
        PRISONER_NUMBER,
        123,
        "prisoner",
        "middle",
        "lastName",
        LocalDate.of(1988, 3, 4),
      )

    val PHYSICAL_ATTRIBUTES_SYNC_REQUEST = PhysicalAttributesSyncRequest(
      PRISONER_HEIGHT,
      PRISONER_WEIGHT,
      appliesFrom = NOW,
      appliesTo = null,
      createdAt = NOW,
      createdBy = USER1,
    )
  }
}
