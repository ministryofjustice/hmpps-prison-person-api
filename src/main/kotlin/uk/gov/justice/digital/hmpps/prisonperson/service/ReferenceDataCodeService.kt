package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository

@Service
@Transactional(readOnly = true)
class ReferenceDataCodeService(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) {
  fun getReferenceDataCodes(domain: String, includeInactive: Boolean): Collection<ReferenceDataCodeDto> =
    TODO("Awaiting DB implementation") // referenceDataCodeRepository.findAllByDomainAndIncludeInactive(domain, includeInactive).toDtos()

  fun getReferenceDataCode(domain: String, code: String): ReferenceDataCodeDto =
    TODO("Awaiting DB implementation") // referenceDataCodeRepository.findByIdAndDomain(code, domain).getOrNull()?.toDto()
}
