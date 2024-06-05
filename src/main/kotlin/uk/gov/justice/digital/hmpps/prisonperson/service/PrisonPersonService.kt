package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.PrisonPersonDataNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.PrisonPersonDto

@Service
@Transactional(readOnly = true)
class PrisonPersonService(
  private val physicalAttributesService: PhysicalAttributesService,
) {
  fun getPrisonPersonData(prisonerNumber: String): PrisonPersonDto? {
    val physicalAttributes = physicalAttributesService.getPhysicalAttributes(prisonerNumber)
      ?: throw PrisonPersonDataNotFoundException(prisonerNumber)

    return PrisonPersonDto(prisonerNumber, physicalAttributes)
  }
}
