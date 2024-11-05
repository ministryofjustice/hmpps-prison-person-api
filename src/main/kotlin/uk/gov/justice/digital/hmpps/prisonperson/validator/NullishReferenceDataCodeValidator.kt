package uk.gov.justice.digital.hmpps.prisonperson.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.Nullish
import kotlin.jvm.optionals.getOrNull

@Service
class NullishReferenceDataCodeValidator(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) :
  ConstraintValidator<NullishReferenceDataCode, Nullish<out String>> {

  private var validDomains = emptyList<String>()

  private var allowNull = true

  override fun initialize(constraintAnnotation: NullishReferenceDataCode) {
    this.validDomains = constraintAnnotation.domains.toList()
    this.allowNull = constraintAnnotation.allowNull
  }

  override fun isValid(value: Nullish<out String>?, context: ConstraintValidatorContext?): Boolean =
    when (value) {
      Nullish.Undefined -> true
      is Nullish.Defined -> {
        if (value.value == null) {
          this.allowNull
        } else {
          val referenceDataCode = referenceDataCodeRepository.findById(value.value).getOrNull()

          validDomains.contains(referenceDataCode?.domain?.code)
        }
      }

      null -> false
    }
}
