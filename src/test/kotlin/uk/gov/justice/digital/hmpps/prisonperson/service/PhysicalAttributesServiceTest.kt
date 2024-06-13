package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.UpdatePhysicalAttributesRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PhysicalAttributesServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

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
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, UPDATE_PHYSICAL_ATTRIBUTES_REQUEST))
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

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, UPDATE_PHYSICAL_ATTRIBUTES_REQUEST))
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

    val NOW: ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/London"))

    val UPDATE_PHYSICAL_ATTRIBUTES_REQUEST = UpdatePhysicalAttributesRequest(PRISONER_HEIGHT, PRISONER_WEIGHT)
  }
}
