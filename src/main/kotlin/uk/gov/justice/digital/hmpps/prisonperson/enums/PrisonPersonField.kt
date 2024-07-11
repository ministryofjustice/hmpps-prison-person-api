package uk.gov.justice.digital.hmpps.prisonperson.enums

enum class PrisonPersonField(val get: (FieldValues) -> Any?, val set: (FieldValues, Any?) -> Unit) {
  HEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }),
  WEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }),
  ;

  fun hasChangedFrom(old: FieldValues, new: Any?) = new != get(old)
}

interface FieldValues {
  var valueInt: Int?
}
