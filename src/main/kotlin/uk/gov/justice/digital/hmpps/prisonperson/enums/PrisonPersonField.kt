package uk.gov.justice.digital.hmpps.prisonperson.enums

enum class PrisonPersonField(val get: (FieldValues) -> Any?, val set: (FieldValues, Any?) -> Unit) {
  HEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }),
  WEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }),
  HAIR({ it.valueString }, { values, value -> values.valueString = value as String? }),
  FACIAL_HAIR({ it.valueString }, { values, value -> values.valueString = value as String? }),
  FACE({ it.valueString }, { values, value -> values.valueString = value as String? }),
  BUILD({ it.valueString }, { values, value -> values.valueString = value as String? }),
  ;

  fun hasChangedFrom(old: FieldValues, new: Any?) = new != get(old)
}

interface FieldValues {
  var valueInt: Int?
  var valueString: String?
}
