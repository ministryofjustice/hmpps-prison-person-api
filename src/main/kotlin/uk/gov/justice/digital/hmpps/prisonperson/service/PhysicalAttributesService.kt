package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.service.event.Source.DPS
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
      .orElseGet { newPhysicalAttributesFor(prisonerNumber, now) }
      .apply {
        height = request.height
        weight = request.weight
        lastModifiedAt = now
        lastModifiedBy = authenticationFacade.getUserOrSystemInContext()
      }
      .also { it.addToHistory() }
      .also { it.publishUpdateEvent(DPS, now) }

    return physicalAttributesRepository.save(physicalAttributes).toDto()
  }

  private fun newPhysicalAttributesFor(prisonerNumber: String, now: ZonedDateTime): PhysicalAttributes {
    validatePrisonerNumber(prisonerNumber)

    val username = authenticationFacade.getUserOrSystemInContext()
    return PhysicalAttributes(
      prisonerNumber,
      createdAt = now,
      createdBy = username,
      lastModifiedBy = username,
    )
  }

  private fun validatePrisonerNumber(prisonerNumber: String) =
    require(prisonerSearchClient.getPrisoner(prisonerNumber) != null) { "Prisoner number '$prisonerNumber' not found" }
}
