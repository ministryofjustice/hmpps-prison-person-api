package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import java.time.ZonedDateTime
import java.util.*

@Entity
class DistinguishingMarkHistory(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val historyId: Long = -1,

  @ManyToOne
  @JoinColumn(name = "distinguishing_mark_id", referencedColumnName = "distinguishing_mark_id", nullable = false)
  var mark: DistinguishingMark? = null,

  @Convert(converter = DistinguishingMarkConverter::class)
  var valueJson: DistinguishingMark? = null,

  override val appliesFrom: ZonedDateTime = ZonedDateTime.now(),
  override var appliesTo: ZonedDateTime? = null,
  override val createdAt: ZonedDateTime = ZonedDateTime.now(),
  override val createdBy: String,
  override val migratedAt: ZonedDateTime? = null,
  override var mergedAt: ZonedDateTime? = null,
  override var mergedFrom: String? = null,
  override val source: Source? = null,
  override var anomalous: Boolean,
) : HistoryItem
