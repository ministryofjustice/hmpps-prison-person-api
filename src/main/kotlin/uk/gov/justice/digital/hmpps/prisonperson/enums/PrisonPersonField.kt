package uk.gov.justice.digital.hmpps.prisonperson.enums

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.prisonperson.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode

private val hasChangedInt: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueInt }
private val hasChangedString: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueString }
private val hasChangedRef: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueRef }

enum class PrisonPersonField(
  val get: (FieldValues) -> Any?,
  val set: (FieldValues, Any?) -> Unit,
  val domain: String?,
  val hasChangedFrom: (FieldValues, Any?) -> Boolean,
) {
  HEIGHT(
    { it.valueInt },
    { values, value -> values.valueInt = value as Int? },
    "HEIGHT",
    hasChangedInt,
  ),
  WEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }, "WEIGHT", hasChangedInt),
  HAIR({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "HAIR", hasChangedRef),
  FACIAL_HAIR(
    { it.valueRef },
    { values, value -> values.valueRef = value as ReferenceDataCode? },
    "FACIAL_HAIR",
    hasChangedRef,
  ),
  FACE({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "FACE", hasChangedRef),
  BUILD({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "BUILD", hasChangedRef),
  LEFT_EYE_COLOUR(
    { it.valueRef },
    { values, value -> values.valueRef = value as ReferenceDataCode? },
    "EYE",
    hasChangedRef,
  ),
  RIGHT_EYE_COLOUR(
    { it.valueRef },
    { values, value -> values.valueRef = value as ReferenceDataCode? },
    "EYE",
    hasChangedRef,
  ),
  SHOE_SIZE({ it.valueString }, { values, value -> values.valueString = value as String? }, null, hasChangedString),
  SMOKER_OR_VAPER(
    { it.valueRef },
    { values, value -> values.valueRef = value as ReferenceDataCode? },
    "SMOKE",
    hasChangedRef,
  ),
  FOOD_ALLERGY(
    { it.valueJson },
    { values, value ->
      run {
        if (value != null) {
          val objectMapper = jacksonObjectMapper()
          value as MutableSet<FoodAllergy>
          val res = objectMapper.writeValueAsString(value.map { it.allergy.id }.sorted())
          values.valueJson = res
        } else {
          values.valueJson = null
        }
      }
    },
    "FOOD_ALLERGY",
    { old, new ->
      run {
        if (new != null) {
          val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
          val objectMapper = jacksonObjectMapper()
          new as MutableSet<FoodAllergy>
          val res = objectMapper.writeValueAsString(new.map { it.allergy.id }.sorted())
          log.info("!!!!!!!! -- Food allergy -- !!!!!!!!!")
          log.info(old.valueJson)
          log.info(res)
          log.info("!!!!!!!! -- Food allergy -- !!!!!!!!!")
          old.valueJson != res
        } else {
          old.valueJson != new
        }
      }
    },
  ),

  MEDICAL_DIET(
    { it.valueJson },
    { values, value ->
      run {
        if (value != null) {
          val objectMapper = jacksonObjectMapper()
          value as MutableSet<MedicalDietaryRequirement>
          val res = objectMapper.writeValueAsString(value.map { it.dietaryRequirement.id }.sorted())
          values.valueJson = res
        } else {
          values.valueJson = null
        }
      }
    },
    "MEDICAL_DIET",
    { old, new ->
      run {
        if (new != null) {
          val objectMapper = jacksonObjectMapper()
          new as MutableSet<MedicalDietaryRequirement>
          val res = objectMapper.writeValueAsString(new.map { it.dietaryRequirement.id }.sorted())
          old.valueJson != res
        } else {
          old.valueJson != new
        }
      }
    },
  ),
  ;
}

interface FieldValues {
  var valueInt: Int?
  var valueString: String?
  var valueRef: ReferenceDataCode?
  var valueJson: String?
}
