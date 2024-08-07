package uk.gov.justice.digital.hmpps.prisonperson.enums

enum class PrisonPersonField(val get: (FieldValues) -> Any?, val set: (FieldValues, Any?) -> Unit) {
  HEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }),
  WEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }),
  HAIR({ it.valueRef }, { values, value -> values.valueRef = value as String? }),
  FACIAL_HAIR({ it.valueRef }, { values, value -> values.valueRef = value as String? }),
  FACE({ it.valueRef }, { values, value -> values.valueRef = value as String? }),
  BUILD({ it.valueRef }, { values, value -> values.valueRef = value as String? }),
  LEFT_EYE_COLOUR({ it.valueRef }, { values, value -> values.valueRef = value as String? }),
  RIGHT_EYE_COLOUR({ it.valueRef }, { values, value -> values.valueRef = value as String? }),
  SHOE_SIZE({ it.valueString }, { values, value -> values.valueString = value as String? }),
  ;

  fun hasChangedFrom(old: FieldValues, new: Any?) = new != get(old)
}

interface FieldValues {
  var valueInt: Int?
  var valueString: String?
  var valueRef: String?
}
