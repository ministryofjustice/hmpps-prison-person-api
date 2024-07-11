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
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PhysicalAttributesUpdatedEvent
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

  // Stores snapshots of each update to a prisoner's physical attributes
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  val fieldHistory: SortedSet<FieldHistory> = sortedSetOf(),

  // Stores timestamps of when each individual field was changed
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @MapKey(name = "field")
  val fieldMetadata: MutableMap<PrisonPersonField, FieldMetadata> = mutableMapOf(),

) : AbstractAggregateRoot<PhysicalAttributes>() {

  private fun fields(): Map<PrisonPersonField, () -> Any?> = mapOf(
    HEIGHT to ::height,
    WEIGHT to ::weight,
  )

  fun toDto(): PhysicalAttributesDto = PhysicalAttributesDto(height, weight)

  fun updateFieldHistory(lastModifiedAt: ZonedDateTime, lastModifiedBy: String, source: Source = DPS) {
    fields().forEach { (field, currentValue) ->
      val previousVersion = fieldHistory.lastOrNull { it.field == field }
      if (previousVersion == null || field.hasChangedFrom(previousVersion, currentValue())) {
        fieldMetadata[field] = FieldMetadata(
          field = field,
          prisonerNumber = this.prisonerNumber,
          lastModifiedAt = lastModifiedAt,
          lastModifiedBy = lastModifiedBy,
        )

        // Set appliesTo on previous history item if not already set
        previousVersion
          ?.takeIf { it.appliesTo == null }
          ?.let { it.appliesTo = lastModifiedAt }

        fieldHistory.add(
          FieldHistory(
            prisonerNumber = this.prisonerNumber,
            field = field,
            appliesFrom = lastModifiedAt,
            createdAt = lastModifiedAt,
            createdBy = lastModifiedBy,
            source = source,
          ).also { field.set(it, currentValue()) },
        )
      }
    }
  }

  fun publishUpdateEvent(source: Source, now: ZonedDateTime) {
    registerEvent(
      PhysicalAttributesUpdatedEvent(
        prisonerNumber = prisonerNumber,
        occurredAt = now,
        source = source,
      ),
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as PhysicalAttributes

    if (prisonerNumber != other.prisonerNumber) return false
    if (height != other.height) return false
    if (weight != other.weight) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + (height ?: 0)
    result = 31 * result + (weight ?: 0)
    return result
  }
}
