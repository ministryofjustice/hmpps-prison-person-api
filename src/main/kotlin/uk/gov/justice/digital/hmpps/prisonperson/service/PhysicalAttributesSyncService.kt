package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.time.Clock
import java.time.ZonedDateTime

@Service
@Transactional
class PhysicalAttributesSyncService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val fieldHistoryRepository: FieldHistoryRepository,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val clock: Clock,
) {
  fun sync(
    prisonerNumber: String,
    request: PhysicalAttributesSyncRequest,
  ): PhysicalAttributesDto = if (request.appliesTo === null) {
    syncLatestPhysicalAttributes(prisonerNumber, request)
  } else {
    syncHistoricalPhysicalAttributes(prisonerNumber, request)
  }

  private fun syncLatestPhysicalAttributes(prisonerNumber: String, request: PhysicalAttributesSyncRequest): PhysicalAttributesDto {
    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { newPhysicalAttributesFor(prisonerNumber) }
      .apply {
        height = request.height
        weight = request.weight
      }
      .also { it.updateFieldHistory(request.createdAt, request.createdBy) }
      .also { it.publishUpdateEvent(NOMIS, now) }

    return physicalAttributesRepository.save(physicalAttributes).toDto()
  }

  private fun syncHistoricalPhysicalAttributes(prisonerNumber: String, request: PhysicalAttributesSyncRequest): PhysicalAttributesDto {
    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet {
        physicalAttributesRepository.save(
          newPhysicalAttributesFor(prisonerNumber).apply {
            height = request.height
            weight = request.weight
          },
        )
      }

    request.addToHistory(prisonerNumber)

    return physicalAttributes.toDto()
  }

  private fun PhysicalAttributesSyncRequest.addToHistory(prisonerNumber: String) {
    val fieldsToSync = mapOf(
      HEIGHT to ::height,
      WEIGHT to ::weight,
    )

    fieldsToSync.onEach { (field, getter) ->
      fieldHistoryRepository.save(
        FieldHistory(
          prisonerNumber = prisonerNumber,
          field = field,
          appliesFrom = appliesFrom,
          appliesTo = appliesTo,
          createdAt = createdAt,
          createdBy = createdBy,
        ).also { field.set(it, getter()) },
      )
    }
  }

  private fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes {
    validatePrisonerNumber(prisonerNumber)
    return PhysicalAttributes(prisonerNumber)
  }

  private fun validatePrisonerNumber(prisonerNumber: String) =
    require(prisonerSearchClient.getPrisoner(prisonerNumber) != null) { "Prisoner number '$prisonerNumber' not found" }
}
