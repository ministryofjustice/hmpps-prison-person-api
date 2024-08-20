package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.*
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.FetchType.LAZY
import org.hibernate.Hibernate
import org.hibernate.annotations.SortNatural
import org.springframework.data.domain.AbstractAggregateRoot
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.*
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toDto
import java.time.ZonedDateTime
import java.util.SortedSet
import kotlin.reflect.KMutableProperty0

@Entity
class Health(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "smoker_or_vaper", referencedColumnName = "id")
  var smokerOrVaper: ReferenceDataCode? = null,

  // Stores snapshots of each update to a prisoner's physical attributes
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  val fieldHistory: SortedSet<FieldHistory> = sortedSetOf(),

  // Stores timestamps of when each individual field was changed
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @MapKey(name = "field")
  val fieldMetadata: MutableMap<PrisonPersonField, FieldMetadata> = mutableMapOf(),
) : AbstractAggregateRoot<Health>() {

  private fun fieldAccessors(): Map<PrisonPersonField, KMutableProperty0<*>> = mapOf(
    SMOKER_OR_VAPER to ::smokerOrVaper,
  )

  fun toDto(): HealthDto = HealthDto(
    smokerOrVaper = getRefDataValueWithMetadata(::smokerOrVaper, SMOKER_OR_VAPER)
  )

  fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
    source: Source = DPS,
    fields: Collection<PrisonPersonField> = allFields,
  ) = updateFieldHistory(lastModifiedAt, lastModifiedAt, lastModifiedBy, source, fields)

  fun updateFieldHistory(
    appliesFrom: ZonedDateTime,
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
    source: Source = DPS,
    fields: Collection<PrisonPersonField>,
    migratedAt: ZonedDateTime? = null,
  ) {
    fieldAccessors()
      .filter { fields.contains(it.key) }
      .forEach { (field, currentValue) ->
        val previousVersion = fieldHistory.lastOrNull { it.field == field }
        if (previousVersion == null ||
          field.hasChangedFrom(
            previousVersion,
            (currentValue() as? ReferenceDataCode)?.id ?: currentValue(),
          )
        ) {
          fieldMetadata[field] = FieldMetadata(
            field = field,
            prisonerNumber = this.prisonerNumber,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy,
          )

          // Set appliesTo on previous history item if not already set
          previousVersion
            ?.takeIf { it.appliesTo == null }
            ?.let { it.appliesTo = appliesFrom }

          fieldHistory.add(
            FieldHistory(
              prisonerNumber = this.prisonerNumber,
              field = field,
              appliesFrom = appliesFrom,
              createdAt = lastModifiedAt,
              createdBy = lastModifiedBy,
              source = source,
              migratedAt = migratedAt,
            ).also { field.set(it, (currentValue() as? ReferenceDataCode)?.id ?: currentValue()) },
          )
        }
      }
  }

  private fun getRefDataValueWithMetadata(
    value: KMutableProperty0<ReferenceDataCode?>,
    field: PrisonPersonField,
  ): ValueWithMetadata<ReferenceDataCodeDto?>? =
    fieldMetadata[field]?.let {
      ValueWithMetadata(
        value.get()?.toDto(),
        it.lastModifiedAt,
        it.lastModifiedBy,
      )
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as Health

    if (prisonerNumber != other.prisonerNumber) return false
    if (smokerOrVaper != other.smokerOrVaper) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + smokerOrVaper.hashCode()
    return result
  }

  companion object {
    val allFields = listOf(
      SMOKER_OR_VAPER,
    )
  }
}
