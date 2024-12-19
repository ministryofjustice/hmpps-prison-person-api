package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.justice.digital.hmpps.prisonperson.dto.history.DistinguishingMarkHistoryDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import java.time.ZonedDateTime

@Entity
class DistinguishingMarkHistory(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val historyId: Long? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "distinguishing_mark_id", referencedColumnName = "distinguishing_mark_id", nullable = false)
  var mark: DistinguishingMark? = null,

  @Convert(converter = DistinguishingMarkConverter::class)
  var valueJson: DistinguishingMarkHistoryDto? = null,

  override val appliesFrom: ZonedDateTime = ZonedDateTime.now(),
  override var appliesTo: ZonedDateTime? = null,
  override val createdAt: ZonedDateTime = ZonedDateTime.now(),
  override val createdBy: String,
  override val migratedAt: ZonedDateTime? = null,
  override var mergedAt: ZonedDateTime? = null,
  override var mergedFrom: String? = null,

  @Enumerated(STRING)
  override val source: Source? = null,
  override var anomalous: Boolean,
) : HistoryItem {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DistinguishingMarkHistory

    if (anomalous != other.anomalous) return false
    if (mark != other.mark) return false
    if (valueJson != other.valueJson) return false
    if (appliesFrom != other.appliesFrom) return false
    if (appliesTo != other.appliesTo) return false
    if (createdAt != other.createdAt) return false
    if (createdBy != other.createdBy) return false
    if (migratedAt != other.migratedAt) return false
    if (mergedAt != other.mergedAt) return false
    if (mergedFrom != other.mergedFrom) return false
    if (source != other.source) return false

    return true
  }

  override fun hashCode(): Int {
    var result = anomalous.hashCode()
    result = 31 * result + (mark?.hashCode() ?: 0)
    result = 31 * result + (valueJson?.hashCode() ?: 0)
    result = 31 * result + appliesFrom.hashCode()
    result = 31 * result + (appliesTo?.hashCode() ?: 0)
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + createdBy.hashCode()
    result = 31 * result + (migratedAt?.hashCode() ?: 0)
    result = 31 * result + (mergedAt?.hashCode() ?: 0)
    result = 31 * result + (mergedFrom?.hashCode() ?: 0)
    result = 31 * result + (source?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "DistinguishingMarkHistory(anomalous=$anomalous, source=$source, mergedFrom=$mergedFrom, mergedAt=$mergedAt, migratedAt=$migratedAt, createdBy='$createdBy', createdAt=$createdAt, appliesTo=$appliesTo, appliesFrom=$appliesFrom, valueJson=$valueJson, markId=${mark?.distinguishingMarkId}, historyId=$historyId)"
  }
}
