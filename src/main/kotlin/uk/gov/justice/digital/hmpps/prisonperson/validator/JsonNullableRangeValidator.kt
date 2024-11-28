package uk.gov.justice.digital.hmpps.prisonperson.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableRange

class JsonNullableRangeValidator : ConstraintValidator<JsonNullableRange, Int?> {

  private var min: Int = 0
  private var max: Int = 0

  override fun initialize(constraintAnnotation: JsonNullableRange) {
    this.min = constraintAnnotation.min
    this.max = constraintAnnotation.max
  }

  override fun isValid(
    value: Int?,
    context: ConstraintValidatorContext?,
  ): Boolean {
    return value == null || value in min..max
  }
}
