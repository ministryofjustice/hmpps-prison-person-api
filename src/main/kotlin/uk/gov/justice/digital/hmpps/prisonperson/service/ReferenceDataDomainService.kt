package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.ReferenceDataDomainNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataDomainDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataDomainRepository
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toDto

@Service
@Transactional(readOnly = true)
class ReferenceDataDomainService(
  private val referenceDataDomainRepository: ReferenceDataDomainRepository,
) {
  fun getReferenceDataDomains(includeInactive: Boolean, includeSubDomains: Boolean = false): Collection<ReferenceDataDomainDto> =
    referenceDataDomainRepository.findAllByIncludeInactive(includeInactive, includeSubDomains).map { it.toDto() }

  fun getReferenceDataDomain(code: String): ReferenceDataDomainDto =
    referenceDataDomainRepository.findByCode(code)?.toDto()
      ?: throw ReferenceDataDomainNotFoundException(code)
}
