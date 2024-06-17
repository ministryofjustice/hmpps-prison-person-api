package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.Hibernate
import org.hibernate.annotations.SortNatural
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import java.time.ZonedDateTime
import java.util.SortedSet

@Entity
class PhysicalAttributes(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @Column(name = "height_cm")
  var height: Int? = null,

  @Column(name = "weight_kg")
  var weight: Int? = null,

  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
  var lastModifiedAt: ZonedDateTime = ZonedDateTime.now(),
  var lastModifiedBy: String,
  val migratedAt: ZonedDateTime? = null,

  @OneToMany(mappedBy = "physicalAttributes", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  val history: SortedSet<PhysicalAttributesHistory> = sortedSetOf(),

) {
  fun toDto(): PhysicalAttributesDto = PhysicalAttributesDto(height, weight)

  fun addToHistory() {
    val previousEntry = history.lastOrNull()

    // Set appliesTo on previous history item if not already set
    previousEntry
      ?.takeIf { it.appliesTo == null }
      ?.let { it.appliesTo = this.lastModifiedAt }

    history.add(
      PhysicalAttributesHistory(
        physicalAttributes = this,
        height = height,
        weight = weight,
        createdAt = this.lastModifiedAt,
        createdBy = this.lastModifiedBy,
        appliesFrom = this.lastModifiedAt,
      ),
    )
  }

  fun getHistoryAsList() = history.toList()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as PhysicalAttributes

    if (prisonerNumber != other.prisonerNumber) return false
    if (height != other.height) return false
    if (weight != other.weight) return false
    if (createdAt != other.createdAt) return false
    if (createdBy != other.createdBy) return false
    if (lastModifiedAt != other.lastModifiedAt) return false
    if (lastModifiedBy != other.lastModifiedBy) return false
    if (migratedAt != other.migratedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + height.hashCode()
    result = 31 * result + weight.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + (createdBy?.hashCode() ?: 0)
    result = 31 * result + lastModifiedAt.hashCode()
    result = 31 * result + (lastModifiedBy?.hashCode() ?: 0)
    result = 31 * result + (migratedAt?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "PhysicalAttributes(" +
      "prisonerNumber='$prisonerNumber', " +
      "height=$height, " +
      "weight=$weight, " +
      "createdAt=$createdAt, " +
      "createdBy=$createdBy, " +
      "lastModifiedAt=$lastModifiedAt, " +
      "lastModifiedBy=$lastModifiedBy, " +
      "migratedAt=$migratedAt" +
      ")"
  }
}
