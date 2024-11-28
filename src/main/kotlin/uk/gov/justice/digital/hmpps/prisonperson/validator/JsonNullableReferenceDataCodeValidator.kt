package uk.gov.justice.digital.hmpps.prisonperson.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import kotlin.jvm.optionals.getOrNull

@Service
class JsonNullableReferenceDataCodeValidator(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) :
  ConstraintValidator<JsonNullableReferenceDataCode, String?> {

  private var validDomains = emptyList<String>()

  private var allowNull = true

  override fun initialize(constraintAnnotation: JsonNullableReferenceDataCode) {
    this.validDomains = constraintAnnotation.domains.toList()
    this.allowNull = constraintAnnotation.allowNull
  }

  override fun isValid(
    value: String?,
    context: ConstraintValidatorContext?,
  ): Boolean {
    if (value == null && !allowNull) {
      return false
    }

    return value?.let {
      referenceDataCodeRepository.findById(it).getOrNull()?.domain?.code in validDomains
    } ?: allowNull
  }
}
