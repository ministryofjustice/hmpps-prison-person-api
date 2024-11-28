package uk.gov.justice.digital.hmpps.prisonperson.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.prisonperson.validator.JsonNullableRangeValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [JsonNullableRangeValidator::class])
annotation class JsonNullableRange(
  val min: Int,
  val max: Int,
  val message: String = "The value must be within the specified range, null or Undefined.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
