package uk.gov.justice.digital.hmpps.prisonperson.utils

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto.PrisonerDto
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PrisonerNumberUtilsTest {
  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Test
  fun `should not throw exception when prisoner number exists`() {
    val prisonerNumber = PRISONER_NUMBER

    whenever(prisonerSearchClient.getPrisoner(prisonerNumber))
      .thenReturn(PRISONER_DTO)

    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
  }

  @Test
  fun `should throw IllegalArgumentException when prisoner number does not exist`() {
    val prisonerNumber = PRISONER_NUMBER

    whenever(prisonerSearchClient.getPrisoner(prisonerNumber))
      .thenReturn(null)

    assertThatThrownBy { validatePrisonerNumber(prisonerSearchClient, prisonerNumber) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Prisoner number '$prisonerNumber' not found")
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234BC"
    val PRISONER_DTO = PrisonerDto(
      PRISONER_NUMBER,
      123456,
      "John",
      "Xavier",
      "Smith",
      LocalDate.of(1993, 7, 30),
    )
  }
}
