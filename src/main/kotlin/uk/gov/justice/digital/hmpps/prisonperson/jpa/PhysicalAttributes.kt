package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.MapKey
import jakarta.persistence.OneToMany
import org.hibernate.Hibernate
import org.hibernate.annotations.SortNatural
import org.springframework.data.domain.AbstractAggregateRoot
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldName.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldName.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PhysicalAttributesUpdatedEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.Source
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

  // Stores snapshots of each update to a prisoner's physical attributes
  @OneToMany(mappedBy = "physicalAttributes", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  val history: SortedSet<PhysicalAttributesHistory> = sortedSetOf(),

  // Stores timestamps of when each individual field was changed
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @MapKey(name = "field")
  val fieldMetadata: MutableMap<FieldName, FieldMetadata> = mutableMapOf(),

) : AbstractAggregateRoot<PhysicalAttributes>() {

  private fun fieldComparators(): Map<FieldName, FieldComparator<Any>> = mapOf(
    HEIGHT to FieldComparator(::height) { it.height },
    WEIGHT to FieldComparator(::weight) { it.weight },
  )

  fun toDto(): PhysicalAttributesDto = PhysicalAttributesDto(height, weight)

  fun addToHistory() {
    val previousEntry = history.lastOrNull().also { updateFieldMetadata(it) }

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

  fun publishUpdateEvent(source: Source, now: ZonedDateTime) {
    registerEvent(
      PhysicalAttributesUpdatedEvent(
        prisonerNumber = prisonerNumber,
        occurredAt = now,
        source = source,
      ),
    )
  }

  fun createNewFieldMetadata() = updateFieldMetadata(null)

  private fun updateFieldMetadata(previousVersion: PhysicalAttributesHistory?) =
    fieldComparators().forEach { (field, comparator) ->
      if (previousVersion == null || comparator.hasChangedFrom(previousVersion)) {
        fieldMetadata[field] = FieldMetadata(
          field = field,
          prisonerNumber = this.prisonerNumber,
          lastModifiedAt = this.lastModifiedAt,
          lastModifiedBy = this.lastModifiedBy,
        )
      }
    }

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
    result = 31 * result + createdBy.hashCode()
    result = 31 * result + lastModifiedAt.hashCode()
    result = 31 * result + lastModifiedBy.hashCode()
    result = 31 * result + (migratedAt?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String = "PhysicalAttributes(" +
    "prisonerNumber='$prisonerNumber', " +
    "height=$height, " +
    "weight=$weight, " +
    "createdAt=$createdAt, " +
    "createdBy=$createdBy, " +
    "lastModifiedAt=$lastModifiedAt, " +
    "lastModifiedBy=$lastModifiedBy, " +
    "migratedAt=$migratedAt" +
    ")"

  private data class FieldComparator<T>(
    val new: () -> T?,
    val old: (history: PhysicalAttributesHistory) -> T?,
  ) {
    fun hasChangedFrom(history: PhysicalAttributesHistory) = new() != old(history)
  }
}
