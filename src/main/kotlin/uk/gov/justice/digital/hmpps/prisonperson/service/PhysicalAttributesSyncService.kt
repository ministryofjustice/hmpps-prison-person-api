package uk.gov.justice.digital.hmpps.prisonperson.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesSyncDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesSyncResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_SYNCED
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class PhysicalAttributesSyncService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val fieldHistoryRepository: FieldHistoryRepository,
  private val physicalAttributesService: PhysicalAttributesService,
  private val clock: Clock,
) {
  fun getPhysicalAttributes(prisonNumber: String): PhysicalAttributesSyncDto? =
    physicalAttributesRepository.findById(prisonNumber).getOrNull()?.toSyncDto()

  fun sync(
    prisonerNumber: String,
    request: PhysicalAttributesSyncRequest,
  ): PhysicalAttributesSyncResponse = if (request.latestBooking == true) {
    syncLatestPhysicalAttributes(prisonerNumber, request)
  } else {
    syncHistoricalPhysicalAttributes(prisonerNumber, request)
  }

  private fun syncLatestPhysicalAttributes(
    prisonerNumber: String,
    request: PhysicalAttributesSyncRequest,
  ): PhysicalAttributesSyncResponse {
    log.debug("Syncing latest physical attributes update from NOMIS for prisoner: $prisonerNumber")

    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findByIdForUpdate(prisonerNumber)
      .orElseGet {
        physicalAttributesService.ensurePhysicalAttributesPersistedFor(prisonerNumber)
        physicalAttributesRepository.findByIdForUpdate(prisonerNumber).orElseThrow()
      }
      .apply {
        height = request.height
        weight = request.weight
      }

    val changedFields = physicalAttributes.updateFieldHistory(request.createdAt, request.createdBy, NOMIS, fieldsToSync)

    return physicalAttributesRepository.save(physicalAttributes).getLatestFieldHistoryIds()
      .also { physicalAttributes.publishUpdateEvent(PHYSICAL_ATTRIBUTES_SYNCED, NOMIS, now, changedFields, it) }
      .also { physicalAttributesRepository.save(physicalAttributes) } // save() required after publishEvent
      .let { PhysicalAttributesSyncResponse(it) }
  }

  private fun syncHistoricalPhysicalAttributes(
    prisonerNumber: String,
    request: PhysicalAttributesSyncRequest,
  ): PhysicalAttributesSyncResponse {
    log.debug("Syncing historical physical attributes update from NOMIS for prisoner: $prisonerNumber")

    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findByIdForUpdate(prisonerNumber)
      .orElseGet {
        physicalAttributesService.ensurePhysicalAttributesPersistedFor(prisonerNumber)
        physicalAttributesRepository.findByIdForUpdate(prisonerNumber).orElseThrow()
          .apply {
            height = request.height
            weight = request.weight
          }
      }

    return request.addToHistory(prisonerNumber)
      .map { it.fieldHistoryId }
      .also { physicalAttributes.publishUpdateEvent(PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL, NOMIS, now, emptyList(), it) }
      .also { physicalAttributesRepository.save(physicalAttributes) } // save() required after publishEvent
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

  private fun PhysicalAttributes.toSyncDto(): PhysicalAttributesSyncDto =
    PhysicalAttributesSyncDto(
      height = height,
      weight = weight,
      hair = hair?.code,
      facialHair = facialHair?.code,
      face = face?.code,
      build = build?.code,
      leftEyeColour = leftEyeColour?.code,
      rightEyeColour = rightEyeColour?.code,
      shoeSize = shoeSize,
    )

  private fun PhysicalAttributes.getLatestFieldHistoryIds() = listOf(
    fieldHistory.last { it.field == HEIGHT },
    fieldHistory.last { it.field == WEIGHT },
  ).map { it.fieldHistoryId }

  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val fieldsToSync = listOf(HEIGHT, WEIGHT)
  }
}
