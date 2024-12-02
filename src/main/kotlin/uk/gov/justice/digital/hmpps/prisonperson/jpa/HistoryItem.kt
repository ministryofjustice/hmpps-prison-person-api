package uk.gov.justice.digital.hmpps.prisonperson.jpa

import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import java.time.Instant
import java.time.ZonedDateTime

interface HistoryItem : Comparable<HistoryItem> {
  val appliesFrom: ZonedDateTime
  var appliesTo: ZonedDateTime?
  val createdAt: ZonedDateTime
  val createdBy: String
  val migratedAt: ZonedDateTime?
  var mergedAt: ZonedDateTime?
  var mergedFrom: String?
  val source: Source?
  var anomalous: Boolean

  override fun compareTo(other: HistoryItem) =
    compareValuesBy(
      this,
      other,
      { it.appliesTo?.toInstant() ?: if (it.anomalous) it.appliesFrom.toInstant() else Instant.MAX },
      { it.createdAt },
      { it.appliesFrom },
      { it.hashCode() },
    )

  fun updateAppliesTo(newAppliesFrom: ZonedDateTime, newLastModifiedAt: ZonedDateTime) {
    // Set appliesTo on previous history item if not already set
    if (!anomalous && appliesTo == null) {
      appliesTo = if (newAppliesFrom > appliesFrom) newAppliesFrom else newLastModifiedAt
      // If the resulting update to appliesTo causes it to be less than appliesFrom, null it and apply the anomalous flag
      if (appliesFrom > appliesTo) {
        anomalous = true
        appliesTo = null
      }
    }
  }
}