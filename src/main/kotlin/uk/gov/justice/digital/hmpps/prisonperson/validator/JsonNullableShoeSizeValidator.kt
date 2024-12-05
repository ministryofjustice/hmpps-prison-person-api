package uk.gov.justice.digital.hmpps.prisonperson.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableShoeSize

class JsonNullableShoeSizeValidator : ConstraintValidator<JsonNullableShoeSize, String?> {

  private var min: String = "0"
  private var max: String = "0"

  override fun initialize(constraintAnnotation: JsonNullableShoeSize) {
    this.min = constraintAnnotation.min
    this.max = constraintAnnotation.max
  }

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    return value == null || isValidShoeSize(value)
  }

  private fun isValidShoeSize(shoeSize: String): Boolean {
    val validShowSize = """^([1-9]|1[0-9]|2[0-5])(\.5|\.0)?$""".toRegex()
    if (!shoeSize.matches(validShowSize)) {
      return false
    }

    val number = shoeSize.toDouble()
    return number in 1.0..25.0
  }
}
