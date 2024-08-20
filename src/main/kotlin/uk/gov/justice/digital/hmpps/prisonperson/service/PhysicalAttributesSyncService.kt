package uk.gov.justice.digital.hmpps.prisonperson.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.config.trackEvent
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesSyncResponse
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
  private val telemetryClient: TelemetryClient,
  private val clock: Clock,
) {
  fun sync(
    prisonerNumber: String,
    request: PhysicalAttributesSyncRequest,
  ): PhysicalAttributesSyncResponse = if (request.latestBooking == true) {
    syncLatestPhysicalAttributes(prisonerNumber, request)
  } else {
    syncHistoricalPhysicalAttributes(prisonerNumber, request)
  }

  private fun syncLatestPhysicalAttributes(prisonerNumber: String, request: PhysicalAttributesSyncRequest): PhysicalAttributesSyncResponse {
    log.debug("Syncing latest physical attributes update from NOMIS for prisoner: $prisonerNumber")

    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { newPhysicalAttributesFor(prisonerNumber) }
      .apply {
        height = request.height
        weight = request.weight
      }
      .also { it.updateFieldHistory(request.createdAt, request.createdBy, NOMIS, fieldsToSync) }
      .also { it.publishUpdateEvent(NOMIS, now) }

    return physicalAttributesRepository.save(physicalAttributes).getLatestFieldHistoryIds()
      .also { trackSyncEvent(prisonerNumber, it) }
      .let { PhysicalAttributesSyncResponse(it) }
  }

  private fun syncHistoricalPhysicalAttributes(prisonerNumber: String, request: PhysicalAttributesSyncRequest): PhysicalAttributesSyncResponse {
    log.debug("Syncing historical physical attributes update from NOMIS for prisoner: $prisonerNumber")

    physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet {
        physicalAttributesRepository.save(
          newPhysicalAttributesFor(prisonerNumber).apply {
            height = request.height
            weight = request.weight
          },
        )
      }
    return request.addToHistory(prisonerNumber)
      .map { it.fieldHistoryId }
      .also { trackSyncEvent(prisonerNumber, it) }
      .let { PhysicalAttributesSyncResponse(it) }
  }

  private fun PhysicalAttributesSyncRequest.addToHistory(prisonerNumber: String): List<FieldHistory> {
    val fieldsToSync = mapOf(
      HEIGHT to ::height,
      WEIGHT to ::weight,
    )

    return fieldsToSync.map { (field, getter) ->
      fieldHistoryRepository.save(
        FieldHistory(
          prisonerNumber = prisonerNumber,
          field = field,
          appliesFrom = appliesFrom,
          appliesTo = appliesTo,
          createdAt = createdAt,
          createdBy = createdBy,
          source = NOMIS,
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

  private fun PhysicalAttributes.getLatestFieldHistoryIds() = listOf(
    fieldHistory.last { it.field == HEIGHT },
    fieldHistory.last { it.field == WEIGHT },
  ).map { it.fieldHistoryId }

  private fun trackSyncEvent(prisonerNumber: String, fieldHistoryIds: List<Long>) {
    telemetryClient.trackEvent(
      "prison-person-api-physical-attributes-synced",
      mapOf(
        "prisonerNumber" to prisonerNumber,
        "fieldHistoryIds" to fieldHistoryIds.toString(),
      ),
    )
  }

  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val fieldsToSync = listOf(HEIGHT, WEIGHT)
  }
}
