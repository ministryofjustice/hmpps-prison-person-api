package uk.gov.justice.digital.hmpps.prisonperson.enums

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.prisonperson.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode

private val getInt: (FieldValues) -> Int? = { it.valueInt }
private val getString: (FieldValues) -> String? = { it.valueString }
private val getRef: (FieldValues) -> ReferenceDataCode? = { it.valueRef }

private val setInt: (FieldValues, Any?) -> Unit = { values, value -> values.valueInt = value as Int? }
private val setString: (FieldValues, Any?) -> Unit = { values, value -> values.valueString = value as String? }
private val setRef: (FieldValues, Any?) -> Unit = { values, value -> values.valueRef = value as ReferenceDataCode? }

private val hasChangedInt: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueInt }
private val hasChangedString: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueString }
private val hasChangedRef: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueRef }

enum class PrisonPersonField(
  val get: (FieldValues) -> Any?,
  val set: (FieldValues, Any?) -> Unit,
  val hasChangedFrom: (FieldValues, Any?) -> Boolean,
  val domain: String?,
) {
  HEIGHT(getInt, setInt, hasChangedInt, "HEIGHT"),
  WEIGHT(getInt, setInt, hasChangedInt, "WEIGHT"),
  HAIR(getRef, setRef, hasChangedRef, "HAIR"),
  FACIAL_HAIR(getRef, setRef, hasChangedRef, "FACIAL_HAIR"),
  FACE(getRef, setRef, hasChangedRef, "FACE"),
  BUILD(getRef, setRef, hasChangedRef, "BUILD"),
  LEFT_EYE_COLOUR(getRef, setRef, hasChangedRef, "EYE"),
  RIGHT_EYE_COLOUR(getRef, setRef, hasChangedRef, "EYE"),
  SHOE_SIZE(getString, setString, hasChangedString, null),
  SMOKER_OR_VAPER(getRef, setRef, hasChangedRef, "SMOKE"),
  FOOD_ALLERGY(
    { it.valueJson },
    { values, value ->
      run {
        val objectMapper = jacksonObjectMapper()
        value as MutableSet<FoodAllergy>
        val res = objectMapper.writeValueAsString(value.map { it.allergy.id }.sorted())
        values.valueJson = res
      }
    },
    { old, new ->
      run {
        val objectMapper = jacksonObjectMapper()
        new as MutableSet<FoodAllergy>
        val res = objectMapper.writeValueAsString(new.map { it.allergy.id }.sorted())
        old.valueJson != res
      }
    },
    "FOOD_ALLERGY",
  ),

  MEDICAL_DIET(
    { it.valueJson },
    { values, value ->
      run {
        val objectMapper = jacksonObjectMapper()
        value as MutableSet<MedicalDietaryRequirement>
        val res = objectMapper.writeValueAsString(value.map { it.dietaryRequirement.id }.sorted())
        values.valueJson = res
      }
    },
    { old, new ->
      run {
        val objectMapper = jacksonObjectMapper()
        new as MutableSet<MedicalDietaryRequirement>
        val res = objectMapper.writeValueAsString(new.map { it.dietaryRequirement.id }.sorted())
        old.valueJson != res
      }
    },
    "MEDICAL_DIET",
  ),
}

interface FieldValues {
  var valueInt: Int?
  var valueString: String?
  var valueRef: ReferenceDataCode?
  var valueJson: String?
}
