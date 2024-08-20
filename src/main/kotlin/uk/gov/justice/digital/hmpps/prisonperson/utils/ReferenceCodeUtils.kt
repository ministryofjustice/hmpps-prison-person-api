package uk.gov.justice.digital.hmpps.prisonperson.utils

import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository

fun toReferenceDataCode(
  referenceDataCodeRepository: ReferenceDataCodeRepository,
  id: String?,
): ReferenceDataCode? = id?.let {
  referenceDataCodeRepository.findById(it)
    .orElseThrow { IllegalArgumentException("Invalid reference data code: $it") }
}
