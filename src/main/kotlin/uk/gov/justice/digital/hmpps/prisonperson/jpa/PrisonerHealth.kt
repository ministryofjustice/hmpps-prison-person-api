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
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.SortNatural
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SMOKER_OR_VAPER
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import java.time.ZonedDateTime
import java.util.SortedSet
import kotlin.reflect.KMutableProperty0

@Entity
@Table(name = "health")
class PrisonerHealth(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  override val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "smoker_or_vaper", referencedColumnName = "id")
  var smokerOrVaper: ReferenceDataCode? = null,

  @OneToMany(mappedBy = "prisonerNumber", cascade = [ALL], orphanRemoval = true)
  var foodAllergies: MutableSet<FoodAllergy> = mutableSetOf(),

  @OneToMany(mappedBy = "prisonerNumber", cascade = [ALL], orphanRemoval = true)
  var medicalDietaryRequirements: MutableSet<MedicalDietaryRequirement> = mutableSetOf(),

  // Stores snapshots of each update to a prisoner's health information
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  override val fieldHistory: SortedSet<FieldHistory> = sortedSetOf(),

  // Stores timestamps of when each individual field was changed
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @MapKey(name = "field")
  override val fieldMetadata: MutableMap<PrisonPersonField, FieldMetadata> = mutableMapOf(),
) : WithFieldHistory<PrisonerHealth>() {

  override fun fieldAccessors(): Map<PrisonPersonField, KMutableProperty0<*>> = mapOf(
    SMOKER_OR_VAPER to ::smokerOrVaper,
    FOOD_ALLERGY to ::foodAllergies,
    MEDICAL_DIET to ::medicalDietaryRequirements,
  )

  fun toDto(): HealthDto = HealthDto(
    smokerOrVaper = getRefDataValueWithMetadata(::smokerOrVaper, SMOKER_OR_VAPER),
    foodAllergies = getReferenceDataListValueWithMetadata(
      foodAllergies,
      { allergies -> allergies.map { it.allergy } },
      FOOD_ALLERGY,
    ),
    medicalDietaryRequirements = getReferenceDataListValueWithMetadata(
      medicalDietaryRequirements,
      { dietaryRequirements -> dietaryRequirements.map { it.dietaryRequirement } },
      MEDICAL_DIET,
    ),
  )

  override fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
  ) = updateFieldHistory(lastModifiedAt, null, lastModifiedAt, lastModifiedBy, DPS, allFields)

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

  private fun <T> getReferenceDataListValueWithMetadata(
    value: T,
    mapper: (T) -> List<ReferenceDataCode>,
    field: PrisonPersonField,
  ): ValueWithMetadata<List<ReferenceDataSimpleDto>>? = fieldMetadata[field]?.let {
    ValueWithMetadata(
      mapper(value).map { code -> code.toSimpleDto() },
      it.lastModifiedAt,
      it.lastModifiedBy,
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as PrisonerHealth

    if (prisonerNumber != other.prisonerNumber) return false
    if (smokerOrVaper != other.smokerOrVaper) return false
    if (foodAllergies != other.foodAllergies) return false
    if (medicalDietaryRequirements != other.medicalDietaryRequirements) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + smokerOrVaper.hashCode()
    result = 31 * result + foodAllergies.hashCode()
    result = 31 * result + medicalDietaryRequirements.hashCode()
    return result
  }

  companion object {
    val allFields = listOf(
      SMOKER_OR_VAPER,
      MEDICAL_DIET,
      FOOD_ALLERGY,
    )
  }
}
