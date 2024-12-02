package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMark
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMarkHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.HistoryItem
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import java.time.ZonedDateTime

data class HistoryComparison<T>(
  val value: T?,
  val appliesFrom: ZonedDateTime,
  val appliesTo: ZonedDateTime?,
  val createdAt: ZonedDateTime,
  val createdBy: String,
  val source: Source = DPS,
  val migratedAt: ZonedDateTime? = null,
  val anomalous: Boolean? = false,
)

fun expectNoFieldHistoryFor(field: PrisonPersonField, history: Collection<FieldHistory>) {
  val fieldHistory = history.filter { it.field == field }.toList()
  assertThat(fieldHistory).isEmpty()
}

fun <T> assertHistoryItemEqual(
  expected: HistoryComparison<T>,
  actual: HistoryItem,
) {
  assertThat(actual.appliesFrom).isEqualTo(expected.appliesFrom)
  assertThat(actual.appliesTo).isEqualTo(expected.appliesTo)
  assertThat(actual.createdAt).isEqualTo(expected.createdAt)
  assertThat(actual.createdBy).isEqualTo(expected.createdBy)
  assertThat(actual.source).isEqualTo(expected.source)
  assertThat(actual.anomalous).isEqualTo(expected.anomalous)
}

fun expectDistinguishingMarksHistory(
  history: Collection<DistinguishingMarkHistory>,
  vararg comparison: HistoryComparison<DistinguishingMark>,
) {
  assertThat(history).hasSize(comparison.size)
  history.forEachIndexed { index, actual ->
    val expected = comparison[index]
    assertThat(actual.mark?.distinguishingMarkId).isEqualTo(expected.value?.distinguishingMarkId)
    assertThat(actual.valueJson).isEqualTo(expected.value?.toHistoryDto())
    assertHistoryItemEqual(expected, actual)
  }
}

// Refactor this for history items so that history item tests can all use it regardless of the fields
fun <T> expectFieldHistory(
  field: PrisonPersonField,
  history: Collection<FieldHistory>,
  vararg comparison: HistoryComparison<T>,
) {
  val fieldHistory = history.filter { it.field == field }.toList()
  assertThat(fieldHistory).hasSize(comparison.size)
  fieldHistory.forEachIndexed { index, actual ->
    val expected = comparison[index]
    assertThat(actual.field).isEqualTo(field)
    assertThat(field.get(actual)).isEqualTo(expected.value)
    assertHistoryItemEqual(expected, actual)
  }
}

fun DistinguishingMark.expectHistory(vararg comparison: HistoryComparison<DistinguishingMark>) =
  expectDistinguishingMarksHistory(history, *comparison)

fun <T> PhysicalAttributes.expectFieldHistory(field: PrisonPersonField, vararg comparison: HistoryComparison<T>) =
  expectFieldHistory(field, fieldHistory, *comparison)
