package uk.gov.justice.digital.hmpps.prisonperson.enums

import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode

enum class PrisonPersonField(
  val get: (FieldValues) -> Any?,
  val set: (FieldValues, Any?) -> Unit,
  val domain: String?,
) {
  HEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }, "HEIGHT"),
  WEIGHT({ it.valueInt }, { values, value -> values.valueInt = value as Int? }, "WEIGHT"),
  HAIR({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "HAIR"),
  FACIAL_HAIR({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "FACIAL_HAIR"),
  FACE({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "FACE"),
  BUILD({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "BUILD"),
  LEFT_EYE_COLOUR({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "EYE"),
  RIGHT_EYE_COLOUR({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "EYE"),
  SHOE_SIZE({ it.valueString }, { values, value -> values.valueString = value as String? }, null),
  SMOKER_OR_VAPER({ it.valueRef }, { values, value -> values.valueRef = value as ReferenceDataCode? }, "SMOKE"),
  ;

  fun hasChangedFrom(old: FieldValues, new: Any?) = new != get(old)
}

interface FieldValues {
  var valueInt: Int?
  var valueString: String?
  var valueRef: ReferenceDataCode?
}
