package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class PhysicalAttributesService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
) {
  fun getPhysicalAttributes(prisonNumber: String): PhysicalAttributesDto? {
    return physicalAttributesRepository.findById(prisonNumber).getOrNull()?.toDto()
  }
}
