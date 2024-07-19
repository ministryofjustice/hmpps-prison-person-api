package uk.gov.justice.digital.hmpps.prisonperson.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldMetadataRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.Clock
import java.time.ZonedDateTime

@Service
class PrisonPersonMergeService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val fieldMetadataRepository: FieldMetadataRepository,
  private val fieldHistoryRepository: FieldHistoryRepository,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val authenticationFacade: AuthenticationFacade,
  private val clock: Clock,
) {

  @Transactional
  fun mergeRecords(
    prisonerNumberFrom: String,
    prisonerNumberTo: String,
  ) {
    mergePhysicalAttributes(prisonerNumberFrom, prisonerNumberTo)
  }

  internal fun mergePhysicalAttributes(
    prisonerNumberFrom: String,
    prisonerNumberTo: String,
  ) {
    log.info("Merging physical attributes from prisoner: '$prisonerNumberFrom', into prisoner: '$prisonerNumberTo'")

    val fieldHistoryFrom = fieldHistoryFor(prisonerNumberFrom)
    val fieldHistoryTo = fieldHistoryFor(prisonerNumberTo)

    if (fieldHistoryFrom.isEmpty()) {
      log.info("No physical attribute history to merge from prisoner: '$prisonerNumberFrom'")
      return
    }

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumberTo).orElseGet {
      log.info("No physical attributes exist yet for prisoner: '$prisonerNumberTo', merging into an empty record")
      newPhysicalAttributesFor(prisonerNumberTo)
    }

    PhysicalAttributes.fields().forEach { field ->
      val latestFrom = fieldHistoryFrom.last { it.field == field }
      val latestTo = fieldHistoryTo.lastOrNull { it.field == field }

      if (latestTo == null) {
        merge(latestFrom, prisonerNumberTo)
      } else if (latestFrom > latestTo) {
        merge(latestFrom, prisonerNumberTo) {
          fieldHistoryRepository.saveAndFlush(latestTo.apply { appliesTo ?: run { appliesTo = it.mergedAt } })
        }
        physicalAttributes.set(field, field.get(latestFrom))
        physicalAttributes.fieldMetadata[field] = latestFrom.toMetadata()
      } else {
        merge(latestFrom, prisonerNumberTo) {
          it.appliesTo ?: run { it.appliesTo = ZonedDateTime.now(clock) }
        }
      }

      fieldHistoryFrom
        .filter { it.field == field && it.fieldHistoryId != latestFrom.fieldHistoryId }
        .forEach { merge(it, prisonerNumberTo) }
    }

    physicalAttributesRepository.save(physicalAttributes)
    deletePhysicalAttributesAndMetadata(prisonerNumberFrom)
  }

  private fun fieldHistoryFor(prisonerNumber: String) = fieldHistoryRepository.findAllByPrisonerNumber(prisonerNumber)

  private fun merge(fieldHistory: FieldHistory, prisonerNumberTo: String, update: (fieldHistory: FieldHistory) -> Unit = {}) {
    fieldHistoryRepository.saveAndFlush(
      fieldHistory.apply {
        mergedAt = ZonedDateTime.now(clock)
        mergedFrom = prisonerNumber
        prisonerNumber = prisonerNumberTo
      }.apply(update),
    )
  }

  private fun deletePhysicalAttributesAndMetadata(prisonerNumber: String) {
    fieldMetadataRepository.deleteAllByPrisonerNumber(prisonerNumber)
    physicalAttributesRepository.deleteByPrisonerNumber(prisonerNumber)
  }

  private fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes {
    validatePrisonerNumber(prisonerNumber)
    return PhysicalAttributes(prisonerNumber)
  }

  private fun validatePrisonerNumber(prisonerNumber: String) =
    require(prisonerSearchClient.getPrisoner(prisonerNumber) != null) { "Prisoner number '$prisonerNumber' not found" }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
