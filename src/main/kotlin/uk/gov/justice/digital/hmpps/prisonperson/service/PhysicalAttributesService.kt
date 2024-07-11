package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class PhysicalAttributesService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val authenticationFacade: AuthenticationFacade,
  private val clock: Clock,
) {
  fun getPhysicalAttributes(prisonNumber: String): PhysicalAttributesDto? = physicalAttributesRepository.findById(prisonNumber).getOrNull()?.toDto()

  @Transactional
  fun createOrUpdate(
    prisonerNumber: String,
    request: PhysicalAttributesUpdateRequest,
  ): PhysicalAttributesDto {
    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { newPhysicalAttributesFor(prisonerNumber) }
      .apply {
        height = request.height
        weight = request.weight
      }
      .also { it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext()) }
      .also { it.publishUpdateEvent(DPS, now) }

    return physicalAttributesRepository.save(physicalAttributes).toDto()
  }

  private fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes {
    validatePrisonerNumber(prisonerNumber)
    return PhysicalAttributes(prisonerNumber)
  }

  private fun validatePrisonerNumber(prisonerNumber: String) =
    require(prisonerSearchClient.getPrisoner(prisonerNumber) != null) { "Prisoner number '$prisonerNumber' not found" }
}
