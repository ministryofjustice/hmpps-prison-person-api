package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesHistoryDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributesHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.service.event.Source.NOMIS
import java.time.Clock
import java.time.ZonedDateTime

@Service
@Transactional
class PhysicalAttributesSyncService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val physicalAttributesHistoryRepository: PhysicalAttributesHistoryRepository,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val clock: Clock,
) {
  fun sync(
    prisonerNumber: String,
    request: PhysicalAttributesSyncRequest,
  ): PhysicalAttributesHistoryDto = if (request.appliesTo === null) {
    syncLatestPhysicalAttributes(prisonerNumber, request)
  } else {
    syncHistoricalPhysicalAttributes(prisonerNumber, request)
  }

  private fun syncLatestPhysicalAttributes(prisonerNumber: String, request: PhysicalAttributesSyncRequest): PhysicalAttributesHistoryDto {
    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { newPhysicalAttributesFor(prisonerNumber, request) }
      .apply {
        height = request.height
        weight = request.weight
        lastModifiedAt = request.createdAt
        lastModifiedBy = request.createdBy
      }
      .also { it.addToHistory() }
      .also { it.publishUpdateEvent(NOMIS, now) }

    return physicalAttributesRepository.save(physicalAttributes).history.last.toDto()
  }

  private fun syncHistoricalPhysicalAttributes(prisonerNumber: String, request: PhysicalAttributesSyncRequest): PhysicalAttributesHistoryDto {
    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet {
        physicalAttributesRepository.save(
          newPhysicalAttributesFor(prisonerNumber, request).apply {
            height = request.height
            weight = request.weight
          },
        )
      }

    return physicalAttributesHistoryRepository.save(
      PhysicalAttributesHistory(
        physicalAttributes = physicalAttributes,
        height = request.height,
        weight = request.weight,
        appliesFrom = request.appliesFrom,
        appliesTo = request.appliesTo,
        createdAt = request.createdAt,
        createdBy = request.createdBy,
      ),
    ).toDto()
  }

  private fun newPhysicalAttributesFor(prisonerNumber: String, request: PhysicalAttributesSyncRequest): PhysicalAttributes {
    validatePrisonerNumber(prisonerNumber)

    return PhysicalAttributes(
      prisonerNumber,
      createdAt = request.createdAt,
      createdBy = request.createdBy,
      lastModifiedAt = request.createdAt,
      lastModifiedBy = request.createdBy,
    )
  }

  private fun validatePrisonerNumber(prisonerNumber: String) =
    require(prisonerSearchClient.getPrisoner(prisonerNumber) != null) { "Prisoner number '$prisonerNumber' not found" }
}
