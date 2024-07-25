package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.prisonperson.enums.FieldValues
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import java.time.Instant
import java.time.ZonedDateTime

@Entity
class FieldHistory(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val fieldHistoryId: Long = -1,

  // Allow this to mutate in order to handle merges
  var prisonerNumber: String,

  @Enumerated(STRING)
  @Column(updatable = false, nullable = false)
  val field: PrisonPersonField,

  override var valueInt: Int? = null,
  override var valueString: String? = null,

  val appliesFrom: ZonedDateTime = ZonedDateTime.now(),
  var appliesTo: ZonedDateTime? = null,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
  val migratedAt: ZonedDateTime? = null,
  var mergedAt: ZonedDateTime? = null,
  var mergedFrom: String? = null,

  @Enumerated(STRING)
  val source: Source? = null,

  ) : FieldValues,
  Comparable<FieldHistory> {

  fun toMetadata() = FieldMetadata(
    prisonerNumber = prisonerNumber,
    field = field,
    lastModifiedAt = createdAt,
    lastModifiedBy = createdBy,
  )

  override fun compareTo(other: FieldHistory) =
    compareValuesBy(this, other, { it.appliesTo?.toInstant() ?: Instant.MAX }, { it.createdAt }, { it.hashCode() })

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as FieldHistory

    if (prisonerNumber != other.prisonerNumber) return false
    if (field != other.field) return false
    if (valueInt != other.valueInt) return false
    if (valueString != other.valueString) return false
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
    var result = prisonerNumber.hashCode()
    result = 31 * result + field.hashCode()
    result = 31 * result + (valueInt ?: 0)
    result = 31 * result + (valueString?.hashCode() ?: 0)
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

  override fun toString(): String = "FieldHistory(" +
    "fieldHistoryId=$fieldHistoryId, " +
    "prisonerNumber='$prisonerNumber', " +
    "field=$field, " +
    "valueInt=$valueInt, " +
    "appliesFrom=$appliesFrom, " +
    "appliesTo=$appliesTo, " +
    "createdAt=$createdAt, " +
    "createdBy='$createdBy', " +
    "migratedAt=$migratedAt, " +
    "mergedAt=$mergedAt, " +
    "mergedFrom=$mergedFrom, " +
    "source=$source" +
    ")"
}
