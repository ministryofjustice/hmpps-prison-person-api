package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.UpdatePhysicalAttributesRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PhysicalAttributesServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @InjectMocks
  lateinit var underTest: PhysicalAttributesService

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
  inner class UpdatePhysicalAttributes {
    @Test
    fun `not yet implemented`() {
      assertThatThrownBy {
        underTest.update(PRISONER_NUMBER, UPDATE_PHYSICAL_ATTRIBUTES_REQUEST)
      }.isInstanceOf(NotImplementedError::class.java)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70

    val UPDATE_PHYSICAL_ATTRIBUTES_REQUEST = UpdatePhysicalAttributesRequest(PRISONER_HEIGHT, PRISONER_WEIGHT)
  }
}
