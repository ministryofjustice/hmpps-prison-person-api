package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import java.time.ZonedDateTime

data class HistoryComparison<T>(
  val value: T?,
  val appliesFrom: ZonedDateTime,
  val appliesTo: ZonedDateTime?,
  val createdAt: ZonedDateTime,
  val createdBy: String,
  val migratedAt: ZonedDateTime? = null,
)

fun <T> expectFieldHistory(field: PrisonPersonField, history: Collection<FieldHistory>, vararg comparison: HistoryComparison<T>) {
  val fieldHistory = history.filter { it.field == field }.toList()
  assertThat(fieldHistory).hasSize(comparison.size)

  fieldHistory.forEachIndexed { index, actual ->
    val expected = comparison[index]
    assertThat(actual.field).isEqualTo(field)
    assertThat(field.get(actual)).isEqualTo(expected.value)
    assertThat(actual.appliesFrom).isEqualTo(expected.appliesFrom)
    assertThat(actual.appliesTo).isEqualTo(expected.appliesTo)
    assertThat(actual.createdAt).isEqualTo(expected.createdAt)
    assertThat(actual.createdBy).isEqualTo(expected.createdBy)
  }
}

fun <T> PhysicalAttributes.expectFieldHistory(field: PrisonPersonField, vararg comparison: HistoryComparison<T>) = expectFieldHistory(field, fieldHistory, *comparison)
