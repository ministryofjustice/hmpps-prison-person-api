package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKey
import jakarta.persistence.OneToMany
import org.hibernate.Hibernate
import org.hibernate.annotations.SortNatural
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SMOKER_OR_VAPER
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import java.time.ZonedDateTime
import java.util.SortedSet
import kotlin.reflect.KMutableProperty0

@Entity
class Health(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  override val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "smoker_or_vaper", referencedColumnName = "id")
  var smokerOrVaper: ReferenceDataCode? = null,

  // Stores snapshots of each update to a prisoner's health information
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  override val fieldHistory: SortedSet<FieldHistory> = sortedSetOf(),

  // Stores timestamps of when each individual field was changed
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @MapKey(name = "field")
  override val fieldMetadata: MutableMap<PrisonPersonField, FieldMetadata> = mutableMapOf(),
) : WithFieldHistory<Health>() {

  override fun fieldAccessors(): Map<PrisonPersonField, KMutableProperty0<*>> = mapOf(
    SMOKER_OR_VAPER to ::smokerOrVaper,
  )

  fun toDto(): HealthDto = HealthDto(
    smokerOrVaper = getRefDataValueWithMetadata(::smokerOrVaper, SMOKER_OR_VAPER),
  )

  override fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
  ) = updateFieldHistory(lastModifiedAt, lastModifiedAt, lastModifiedBy, DPS, allFields)

  private fun getRefDataValueWithMetadata(
    value: KMutableProperty0<ReferenceDataCode?>,
    field: PrisonPersonField,
  ): ValueWithMetadata<ReferenceDataSimpleDto?>? =
    fieldMetadata[field]?.let {
      ValueWithMetadata(
        value.get()?.toSimpleDto(),
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
