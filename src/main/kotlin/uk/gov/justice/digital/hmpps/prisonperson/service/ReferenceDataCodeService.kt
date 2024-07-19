package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.ReferenceDataCodeNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toDto

@Service
@Transactional(readOnly = true)
class ReferenceDataCodeService(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) {

  /*
   * Exclude certain reference data codes
   */
  private val excludedCodes = setOf(
    Pair("FACIAL_HAIR", "NA"),
    Pair("EYE", "MISSING"),
  )

  fun getReferenceDataCodes(domain: String, includeInactive: Boolean): Collection<ReferenceDataCodeDto> =
    referenceDataCodeRepository.findAllByDomainCodeAndIncludeInactive(domain, includeInactive)
      .filterNot { excludedCodes.contains(Pair(it.domain.code, it.code)) }
      .map { it.toDto() }

  fun getReferenceDataCode(code: String, domain: String): ReferenceDataCodeDto =
    referenceDataCodeRepository.findByCodeAndDomainCode(code, domain)?.toDto()
      ?: throw ReferenceDataCodeNotFoundException(code, domain)
}
