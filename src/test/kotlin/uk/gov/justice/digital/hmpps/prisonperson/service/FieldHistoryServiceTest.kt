package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.FieldHistoryDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import java.time.Clock
import java.time.ZonedDateTime
import java.util.SortedSet
import java.util.TreeSet

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class FieldHistoryServiceTest {
  @Mock
  lateinit var fieldHistoryRepository: FieldHistoryRepository

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: FieldHistoryService

  @Nested
  inner class GetFieldHistory {
    @Test
    fun `field history not found`() {
      whenever(
        fieldHistoryRepository.findAllByPrisonerNumberAndField(
          PRISONER_NUMBER,
          FIELD,
        ),
      ).thenReturn(FIELD_HISTORY_EMPTY)

      assertThat(underTest.getFieldHistory(PRISONER_NUMBER, FIELD_NAME)).isEqualTo(listOf<FieldHistoryDto>())
    }

    @Test
    fun `field history retrieved`() {
      whenever(
        fieldHistoryRepository.findAllByPrisonerNumberAndField(
          PRISONER_NUMBER,
          FIELD,
        ),
      ).thenReturn(FIELD_HISTORY)

      assertThat(underTest.getFieldHistory(PRISONER_NUMBER, FIELD_NAME))
        .isEqualTo(FIELD_HISTORY_DTOS)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val FIELD_NAME = "HEIGHT"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val FIELD: PrisonPersonField = PrisonPersonField.valueOf(FIELD_NAME)
    val NOW: ZonedDateTime = ZonedDateTime.now()
    val THEN: ZonedDateTime = NOW.minusDays(1)

    val FIELD_HISTORY_EMPTY: SortedSet<FieldHistory> = TreeSet<FieldHistory>(compareBy { it.fieldHistoryId })

    val FIELD_HISTORY: SortedSet<FieldHistory> = TreeSet<FieldHistory>(compareBy { it.fieldHistoryId }).apply {
      addAll(
        setOf(
          FieldHistory(
            fieldHistoryId = 1,
            prisonerNumber = PRISONER_NUMBER,
            field = FIELD,
            valueInt = 189,
            appliesFrom = THEN,
            appliesTo = NOW,
            createdAt = THEN,
            createdBy = USER1,
            source = Source.DPS,
            anomalous = false,
          ),
          FieldHistory(
            fieldHistoryId = 2,
            prisonerNumber = PRISONER_NUMBER,
            field = FIELD,
            valueInt = 190,
            appliesFrom = NOW,
            createdAt = NOW,
            createdBy = USER2,
            source = Source.DPS,
            anomalous = false,
          ),
        ),
      )
    }

    val FIELD_HISTORY_DTOS = listOf(
      FieldHistoryDto(
        prisonerNumber = "A1234AA",
        field = FIELD,
        valueInt = 189,
        appliesFrom = THEN,
        appliesTo = NOW,
        createdAt = THEN,
        createdBy = "USER1",
        source = Source.DPS.toString(),
        anomalous = false,
      ),
      FieldHistoryDto(
        prisonerNumber = "A1234AA",
        field = FIELD,
        valueInt = 190,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = "USER2",
        source = Source.DPS.toString(),
        anomalous = false,
      ),
    )
  }
}
