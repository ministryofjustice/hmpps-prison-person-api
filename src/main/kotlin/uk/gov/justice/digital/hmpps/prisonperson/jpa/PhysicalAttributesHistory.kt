package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesHistoryDto
import java.time.Instant
import java.time.ZonedDateTime

@Entity
class PhysicalAttributesHistory(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val physicalAttributesHistoryId: Long = -1,

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "prisoner_number")
  val physicalAttributes: PhysicalAttributes,

  @Column(name = "height_cm")
  var height: Int? = null,

  @Column(name = "weight_kg")
  var weight: Int? = null,

  val appliesFrom: ZonedDateTime = ZonedDateTime.now(),
  var appliesTo: ZonedDateTime? = null,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
  val migratedAt: ZonedDateTime? = null,
) : Comparable<PhysicalAttributesHistory> {

  fun toDto(): PhysicalAttributesHistoryDto = PhysicalAttributesHistoryDto(
    physicalAttributesHistoryId,
    height,
    weight,
    appliesFrom,
    appliesTo,
    createdAt,
    createdBy,
  )

  override fun compareTo(other: PhysicalAttributesHistory) =
    compareValuesBy(this, other, { it.appliesTo?.toInstant() ?: Instant.MAX }, { it.createdAt }, { it.hashCode() })

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as PhysicalAttributesHistory

    if (physicalAttributes.prisonerNumber != other.physicalAttributes.prisonerNumber) return false
    if (height != other.height) return false
    if (weight != other.weight) return false
    if (appliesFrom != other.appliesFrom) return false
    if (appliesTo != other.appliesTo) return false
    if (createdAt != other.createdAt) return false
    if (createdBy != other.createdBy) return false
    if (migratedAt != other.migratedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = physicalAttributes.prisonerNumber.hashCode()
    result = 31 * result + (height?.hashCode() ?: 0)
    result = 31 * result + (weight?.hashCode() ?: 0)
    result = 31 * result + appliesFrom.hashCode()
    result = 31 * result + (appliesTo?.hashCode() ?: 0)
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + (createdBy?.hashCode() ?: 0)
    result = 31 * result + (migratedAt?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "PhysicalAttributesHistory(" +
      "prisonerNumber=${physicalAttributes.prisonerNumber}, " +
      "height=$height, " +
      "weight=$weight, " +
      "appliesFrom=$appliesFrom, " +
      "appliesTo=$appliesTo, " +
      "createdAt=$createdAt, " +
      "createdBy=$createdBy, " +
      "migratedAt=$migratedAt" +
      ")"
  }
}
