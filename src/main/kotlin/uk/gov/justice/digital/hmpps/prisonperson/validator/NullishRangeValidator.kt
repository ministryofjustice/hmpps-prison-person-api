package uk.gov.justice.digital.hmpps.prisonperson.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishRange
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.Nullish
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.Nullish.Defined
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.Nullish.Undefined

class NullishRangeValidator : ConstraintValidator<NullishRange, Nullish<out Int>> {

  private var min: Int = 0
  private var max: Int = 0

  override fun initialize(constraintAnnotation: NullishRange) {
    this.min = constraintAnnotation.min
    this.max = constraintAnnotation.max
  }

  override fun isValid(value: Nullish<out Int>, context: ConstraintValidatorContext): Boolean = when (value) {
    Undefined -> true
    is Defined -> value.value == null || value.value in min..max
  }
}
