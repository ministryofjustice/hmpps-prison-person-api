package uk.gov.justice.digital.hmpps.prisonperson.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesMigrationResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_MIGRATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.time.Clock
import java.time.ZonedDateTime
import java.util.SortedSet

@Service
@Transactional
class PhysicalAttributesMigrationService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val physicalAttributesService: PhysicalAttributesService,
  private val clock: Clock,
) {
  fun migrate(
    prisonerNumber: String,
    migration: SortedSet<PhysicalAttributesMigrationRequest>,
  ): PhysicalAttributesMigrationResponse {
    log.info("Attempting to migrate physical attributes for $prisonerNumber")

    if (migration.isEmpty()) {
      log.info("No physical attributes provided for $prisonerNumber")
      return PhysicalAttributesMigrationResponse()
    }

    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { physicalAttributesService.newPhysicalAttributesFor(prisonerNumber) }
      .also { it.resetHistoryForMigratedFields() }

    migration.forEach { record ->
      val appliesTo = record.appliesTo?.takeIf { record.isNestedBooking(migration) }

      physicalAttributes.apply {
        height = record.height
        weight = record.weight
      }.also {
        it.updateFieldHistory(
          appliesFrom = record.appliesFrom,
          appliesTo = appliesTo,
          lastModifiedAt = record.createdAt,
          lastModifiedBy = record.createdBy,
          source = NOMIS,
          fields = fieldsToMigrate,
          migratedAt = now,
        )
      }
    }

    return physicalAttributesRepository.save(physicalAttributes).fieldHistory
      .filter { it.migratedAt == now }
      .map { it.fieldHistoryId }
      .also { physicalAttributes.publishUpdateEvent(PHYSICAL_ATTRIBUTES_MIGRATED, NOMIS, now, fieldsToMigrate, it) }
      .also { physicalAttributesRepository.save(physicalAttributes) } // save() required after publishEvent
      .let { PhysicalAttributesMigrationResponse(it) }
  }

  /*
    This handles the case where there is an overlap in booking dates, and
    we can't define a logical boundary between one attribute being applicable with the next,
    so we explicitly set the appliesTo date.
   */
  private fun PhysicalAttributesMigrationRequest.isNestedBooking(
    others: SortedSet<PhysicalAttributesMigrationRequest>,
  ): Boolean =
    others
      .filterNot { it == this }
      .find { other ->
        this.appliesTo != null &&
          other.appliesFrom <= this.appliesFrom &&
          (other.appliesTo == null || other.appliesTo >= this.appliesTo)
      } != null

  private fun PhysicalAttributes.resetHistoryForMigratedFields() {
    fieldsToMigrate.forEach { field -> fieldHistory.removeIf { it.field == field } }
  }

  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val fieldsToMigrate = listOf(HEIGHT, WEIGHT)
  }
}
