package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.config.PrisonPersonDataNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PrisonPersonDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ValueWithMetadata
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class PrisonPersonServiceTest {
  @Mock
  lateinit var physicalAttributesService: PhysicalAttributesService

  @InjectMocks
  lateinit var underTest: PrisonPersonService

  @Test
  fun `prison person data not found`() {
    whenever(physicalAttributesService.getPhysicalAttributes(PRISONER_NUMBER)).thenReturn(null)
    assertThatThrownBy { underTest.getPrisonPersonData(PRISONER_NUMBER) }
      .isInstanceOf(PrisonPersonDataNotFoundException::class.java)
      .hasMessage("No data for '$PRISONER_NUMBER'")
  }

  @Test
  fun `prison person data retrieved`() {
    whenever(physicalAttributesService.getPhysicalAttributes(PRISONER_NUMBER)).thenReturn(PHYSICAL_ATTRIBUTES)

    val result = underTest.getPrisonPersonData(PRISONER_NUMBER)
    val expected = PrisonPersonDto(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES)

    assertThat(result).isEqualTo(expected)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70
    const val USER1 = "USER1"

    val NOW = ZonedDateTime.now()

    val PHYSICAL_ATTRIBUTES = PhysicalAttributesDto(
      height = ValueWithMetadata(PRISONER_HEIGHT, NOW, USER1),
      weight = ValueWithMetadata(PRISONER_WEIGHT, NOW, USER1),
    )
  }
}
