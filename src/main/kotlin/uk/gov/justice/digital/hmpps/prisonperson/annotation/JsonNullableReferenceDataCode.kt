package uk.gov.justice.digital.hmpps.prisonperson.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.prisonperson.validator.JsonNullableReferenceDataCodeValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [JsonNullableReferenceDataCodeValidator::class])
annotation class JsonNullableReferenceDataCode(
  val domains: Array<String> = [],
  val allowNull: Boolean = true,
  val message: String = "The value must be a reference domain code id of the correct domain, null, or Undefined.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
