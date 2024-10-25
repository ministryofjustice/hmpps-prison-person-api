package uk.gov.justice.digital.hmpps.prisonperson.jpa

import org.springframework.data.domain.AbstractAggregateRoot
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import java.time.ZonedDateTime
import java.util.SortedSet
import kotlin.reflect.KMutableProperty0

abstract class WithFieldHistory<T : AbstractAggregateRoot<T>?> : AbstractAggregateRoot<T>() {
  abstract val prisonerNumber: String
  abstract val fieldHistory: SortedSet<FieldHistory>
  abstract val fieldMetadata: MutableMap<PrisonPersonField, FieldMetadata>
  protected abstract fun fieldAccessors(): Map<PrisonPersonField, KMutableProperty0<*>>

  abstract fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
  ): Collection<PrisonPersonField>

  fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
    source: Source,
    fields: Collection<PrisonPersonField>,
  ): Collection<PrisonPersonField> =
    updateFieldHistory(lastModifiedAt, null, lastModifiedAt, lastModifiedBy, source, fields)

  fun updateFieldHistory(
    appliesFrom: ZonedDateTime,
    appliesTo: ZonedDateTime?,
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
    source: Source = DPS,
    fields: Collection<PrisonPersonField>,
    migratedAt: ZonedDateTime? = null,
    anomalous: Boolean? = null,
  ): Collection<PrisonPersonField> {
    val changedFields = mutableSetOf<PrisonPersonField>()

    fieldAccessors()
      .filter { fields.contains(it.key) }
      .forEach { (field, currentValue) ->
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
            ?.takeIf { !it.anomalous && it.appliesTo == null }
            ?.let {
              it.appliesTo = if (appliesFrom > it.appliesFrom) appliesFrom else lastModifiedAt
              // If the resulting update to appliesTo causes it to be less than appliesFrom, null it and apply the anomalous flag
              if (it.appliesFrom > it.appliesTo) {
                it.anomalous = true
                it.appliesTo = null
              }
            }

          fieldHistory.add(
            FieldHistory(
              prisonerNumber = this.prisonerNumber,
              field = field,
              appliesFrom = appliesFrom,
              appliesTo = appliesTo,
              createdAt = lastModifiedAt,
              createdBy = lastModifiedBy,
              source = source,
              migratedAt = migratedAt,
              anomalous = anomalous == true,
            ).also { field.set(it, currentValue()) },
          )
          changedFields.add(field)
        }
      }
    return changedFields
  }

  public override fun domainEvents(): Collection<Any> = super.domainEvents()
}
