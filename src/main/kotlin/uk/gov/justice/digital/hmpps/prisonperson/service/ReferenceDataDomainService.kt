package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataDomainDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataDomainRepository

@Service
@Transactional(readOnly = true)
class ReferenceDataDomainService(
  private val referenceDataDomainRepository: ReferenceDataDomainRepository,
) {
  fun getReferenceDataDomains(includeInactive: Boolean): Collection<ReferenceDataDomainDto> =
    TODO("Awaiting DB implementation") // referenceDataDomainRepository.findAllByIncludeInactive(includeInactive).toDtos()

  fun getReferenceDataDomain(domain: String): ReferenceDataDomainDto =
    TODO("Awaiting DB implementation") // referenceDataDomainRepository.findById(domain).getOrNull()?.toDto()
}
