package uk.gov.justice.digital.hmpps.prisonperson.utils

import org.openapitools.jackson.nullable.JsonNullable
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository

fun toReferenceDataCode(
  referenceDataCodeRepository: ReferenceDataCodeRepository,
  id: String?,
): ReferenceDataCode? = id?.let {
  referenceDataCodeRepository.findById(it)
    .orElseThrow { IllegalArgumentException("Invalid reference data code: $it") }
}

fun <T> toReferenceDataCodeWithDefault(
  referenceDataCodeRepository: ReferenceDataCodeRepository,
  id: JsonNullable<T>,
  default: ReferenceDataCode?,
): ReferenceDataCode? where T : String? {
  if (!id.isPresent) {
    return default
  }

  return toReferenceDataCode(referenceDataCodeRepository, id.get())
}

fun toReferenceDataCodeId(code: String?, domain: String?): String? = code?.let { c ->
  domain?.let { d ->
    "${d}_$c"
  }
}
