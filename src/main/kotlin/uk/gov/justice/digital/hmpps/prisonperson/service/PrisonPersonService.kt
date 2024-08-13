package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.PrisonPersonDataNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PrisonPersonDto

@Service
@Transactional(readOnly = true)
class PrisonPersonService(
  private val physicalAttributesService: PhysicalAttributesService,
  private val healthService: HealthService,
) {
  fun getPrisonPersonData(prisonerNumber: String): PrisonPersonDto? {
    val physicalAttributes = physicalAttributesService.getPhysicalAttributes(prisonerNumber)
    val health = healthService.getHealth(prisonerNumber)

    if (physicalAttributes == null && health == null) {
      throw PrisonPersonDataNotFoundException(prisonerNumber)
    }

    return PrisonPersonDto(
      prisonerNumber,
      physicalAttributes ?: PhysicalAttributesDto(),
      health ?: HealthDto(),
    )
  }
}
