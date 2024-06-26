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
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PhysicalAttributesServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var authenticationFacade: AuthenticationFacade

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: PhysicalAttributesService

  private val savedPhysicalAttributes = argumentCaptor<PhysicalAttributes>()

  @Nested
  inner class GetPhysicalAttributes {
    @Test
    fun `physical attributes not found`() {
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

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
            createdBy = USER1,
            lastModifiedBy = USER1,
          ),
        ),
      )

      assertThat(underTest.getPhysicalAttributes(PRISONER_NUMBER))
        .isEqualTo(PhysicalAttributesDto(PRISONER_HEIGHT, PRISONER_WEIGHT))
    }
  }

  @Nested
  inner class CreateOrUpdatePhysicalAttributes {

    @BeforeEach
    fun beforeEach() {
      whenever(physicalAttributesRepository.save(savedPhysicalAttributes.capture())).thenAnswer { savedPhysicalAttributes.firstValue }
      whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
    }

    @Test
    fun `creates new physical attributes`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(PRISONER_SEARCH_RESPONSE)
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_UPDATE_REQUEST))
        .isEqualTo(PhysicalAttributesDto(PRISONER_HEIGHT, PRISONER_WEIGHT))

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

      assertThatThrownBy { underTest.createOrUpdate(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_UPDATE_REQUEST) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("Prisoner number '${PRISONER_NUMBER}' not found")

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
            createdBy = USER2,
            lastModifiedAt = NOW.minusDays(1),
            lastModifiedBy = USER2,
          ).also { it.addToHistory() },
        ),
      )

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_UPDATE_REQUEST))
        .isEqualTo(PhysicalAttributesDto(PRISONER_HEIGHT, PRISONER_WEIGHT))

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)
        assertThat(createdAt).isEqualTo(NOW.minusDays(1))
        assertThat(createdBy).isEqualTo(USER2)
        assertThat(lastModifiedAt).isEqualTo(NOW)
        assertThat(lastModifiedBy).isEqualTo(USER1)
        assertThat(migratedAt).isNull()

        assertThat(history).hasSize(2)
        // Initial history entry:
        with(getHistoryAsList()[0]) {
          assertThat(height).isEqualTo(PREVIOUS_PRISONER_HEIGHT)
          assertThat(weight).isEqualTo(PREVIOUS_PRISONER_WEIGHT)
          assertThat(createdAt).isEqualTo(NOW.minusDays(1))
          assertThat(createdBy).isEqualTo(USER2)
          assertThat(appliesFrom).isEqualTo(NOW.minusDays(1))
          assertThat(appliesTo).isEqualTo(NOW)
          assertThat(migratedAt).isNull()
        }
        // New history entry:
        with(getHistoryAsList()[1]) {
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

    val PHYSICAL_ATTRIBUTES_UPDATE_REQUEST = PhysicalAttributesUpdateRequest(PRISONER_HEIGHT, PRISONER_WEIGHT)
    val PRISONER_SEARCH_RESPONSE =
      PrisonerDto(
        PRISONER_NUMBER,
        123,
        "prisoner",
        "middle",
        "lastName",
        LocalDate.of(1988, 3, 4),
      )
  }
}
