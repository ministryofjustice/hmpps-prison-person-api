package uk.gov.justice.digital.hmpps.prisonperson.service.merge

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_MERGED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldMetadataRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.service.PhysicalAttributesService
import java.time.Clock
import java.time.ZonedDateTime
import java.util.SortedSet

@Service
class PhysicalAttributesMergeService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val fieldMetadataRepository: FieldMetadataRepository,
  private val fieldHistoryRepository: FieldHistoryRepository,
  private val physicalAttributesService: PhysicalAttributesService,
  private val clock: Clock,
) : PrisonPersonMerge {

  @Transactional
  override fun mergeRecords(
    prisonerNumberFrom: String,
    prisonerNumberTo: String,
  ) {
    log.debug("Merging physical attributes from prisoner: '$prisonerNumberFrom', into prisoner: '$prisonerNumberTo'")

    val fieldHistoryFrom = fieldHistoryFor(prisonerNumberFrom)
    val fieldHistoryTo = fieldHistoryFor(prisonerNumberTo)

    if (fieldHistoryFrom.isEmpty()) {
      log.info("No physical attribute history to merge from prisoner: '$prisonerNumberFrom'")
      return
    }

    mergePhysicalAttributesFieldHistory(fieldHistoryFrom, fieldHistoryTo, prisonerNumberFrom, prisonerNumberTo)
    deletePhysicalAttributesAndMetadata(prisonerNumberFrom)
  }

  private fun mergePhysicalAttributesFieldHistory(
    historyFrom: SortedSet<FieldHistory>,
    historyTo: SortedSet<FieldHistory>,
    prisonerNumberFrom: String,
    prisonerNumberTo: String,
  ) {
    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumberTo).orElseGet {
      log.debug("No physical attributes exist yet for prisoner: '$prisonerNumberTo', merging into an empty record")
      physicalAttributesService.newPhysicalAttributesFor(prisonerNumberTo)
    }

    fieldsToMerge.forEach { field ->
      val latestFrom = historyFrom.lastOrNull { it.field == field }
      val latestTo = historyTo.lastOrNull { it.field == field }

      if (latestFrom == null) {
        log.debug("Prisoner: '$prisonerNumberFrom' has nothing to merge for field: '$field'")
        return@forEach
      }

      log.debug(
        "Merging field: $field, history id: ${latestFrom.fieldHistoryId} " +
          "from prisoner: '$prisonerNumberFrom', into prisoner: '$prisonerNumberTo'",
      )

      mergeLatest(latestFrom, latestTo, prisonerNumberTo, physicalAttributes)

      historyFrom
        .filterOn(field, excludingId = latestFrom.fieldHistoryId)
        .forEach { merge(it, prisonerNumberTo) }
    }

    physicalAttributes.publishMergeEvent(
      PHYSICAL_ATTRIBUTES_MERGED,
      prisonerNumberFrom,
      prisonerNumberTo,
      DPS,
      now,
      fieldsToMerge,
    )

    physicalAttributesRepository.save(physicalAttributes) // save() required after publishEvent
  }

  private fun mergeLatest(
    latestFrom: FieldHistory,
    latestTo: FieldHistory?,
    prisonerNumberTo: String,
    physicalAttributes: PhysicalAttributes,
  ) {
    merge(latestFrom, prisonerNumberTo)

    if (latestTo == null || latestFrom > latestTo) {
      updatePhysicalAttributesAndMetadata(latestFrom, physicalAttributes)
    }

    if (latestTo != null && latestTo > latestFrom) {
      latestFrom.apply { appliesTo ?: run { appliesTo = this.mergedAt } }
    } else {
      latestTo?.apply { appliesTo ?: run { appliesTo = latestFrom.mergedAt } }
    }
  }

  private fun updatePhysicalAttributesAndMetadata(
    latestFieldHistory: FieldHistory,
    physicalAttributes: PhysicalAttributes,
  ) {
    val field = latestFieldHistory.field
    physicalAttributes.set(field, field.get(latestFieldHistory))
    physicalAttributes.fieldMetadata[field] =
      (physicalAttributes.fieldMetadata[field] ?: latestFieldHistory.toMetadata())
        .apply {
          lastModifiedAt = latestFieldHistory.createdAt
          lastModifiedBy = latestFieldHistory.createdBy
        }
  }

  private fun fieldHistoryFor(prisonerNumber: String) = fieldHistoryRepository.findAllByPrisonerNumber(prisonerNumber)

  private fun merge(fieldHistory: FieldHistory, prisonerNumberTo: String) =
    fieldHistoryRepository.saveAndFlush(
      fieldHistory.apply {
        mergedAt = ZonedDateTime.now(clock)
        mergedFrom = prisonerNumber
        prisonerNumber = prisonerNumberTo
      },
    )

  private fun deletePhysicalAttributesAndMetadata(prisonerNumber: String) {
    fieldMetadataRepository.deleteAllByPrisonerNumber(prisonerNumber)
    physicalAttributesRepository.deleteByPrisonerNumber(prisonerNumber)
  }

  private fun SortedSet<FieldHistory>.filterOn(field: PrisonPersonField, excludingId: Long) =
    filter { it.field == field && it.fieldHistoryId != excludingId }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    val fieldsToMerge =
      listOf(HEIGHT, WEIGHT, HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)
  }
}
