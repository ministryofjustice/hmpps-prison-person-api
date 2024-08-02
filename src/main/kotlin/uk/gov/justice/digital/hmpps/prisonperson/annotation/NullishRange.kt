package uk.gov.justice.digital.hmpps.prisonperson.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.prisonperson.validator.NullishRangeValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NullishRangeValidator::class])
annotation class NullishRange(
  val min: Int,
  val max: Int,
  val message: String = "The value must be within the specified range",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
