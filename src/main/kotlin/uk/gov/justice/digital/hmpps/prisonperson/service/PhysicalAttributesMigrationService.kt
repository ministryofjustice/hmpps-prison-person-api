package uk.gov.justice.digital.hmpps.prisonperson.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.trackEvent
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesMigrationResponse
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
  private val telemetryClient: TelemetryClient,
  private val clock: Clock,
) {
  fun migrate(
    prisonerNumber: String,
    migration: SortedSet<PhysicalAttributesMigrationRequest>,
  ): PhysicalAttributesMigrationResponse {
    log.info("Attempting to migrate physical attributes for $prisonerNumber")

    if (migration.isEmpty()) {
      trackMigrationEvent(prisonerNumber, listOf())
      return PhysicalAttributesMigrationResponse()
    }

    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { newPhysicalAttributesFor(prisonerNumber) }
      .also { it.resetHistoryForMigratedFields() }

    migration.forEach { record ->
      physicalAttributes.apply {
        height = record.height
        weight = record.weight
      }.also {
        it.updateFieldHistory(
          record.appliesFrom,
          record.createdAt,
          record.createdBy,
          source = NOMIS,
          fields = fieldsToMigrate,
          migratedAt = now,
        )
      }
    }

    return physicalAttributesRepository.save(physicalAttributes).fieldHistory
      .filter { it.migratedAt == now }
      .map { it.fieldHistoryId }
      .also { trackMigrationEvent(prisonerNumber, it) }
      .let { PhysicalAttributesMigrationResponse(it) }
  }

  private fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes = PhysicalAttributes(prisonerNumber)

  private fun PhysicalAttributes.resetHistoryForMigratedFields() {
    fieldsToMigrate.forEach { field -> fieldHistory.removeIf { it.field == field } }
  }

  private fun trackMigrationEvent(prisonerNumber: String, fieldHistoryIds: List<Long>) {
    telemetryClient.trackEvent(
      "prison-person-api-physical-attributes-migrated",
      mapOf(
        "prisonerNumber" to prisonerNumber,
        "fieldHistoryIds" to fieldHistoryIds.toString(),
      ),
    )
  }

  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val fieldsToMigrate = listOf(HEIGHT, WEIGHT)
  }
}
