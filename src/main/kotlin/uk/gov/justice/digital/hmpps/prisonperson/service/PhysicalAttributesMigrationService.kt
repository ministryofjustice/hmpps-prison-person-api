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
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
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
    val latestRecord = migration.last()

    val physicalAttributes = latestRecord.let {
      PhysicalAttributes(prisonerNumber, height = it.height, weight = it.weight)
    }

    listOf(HEIGHT, WEIGHT).onEach { field ->
      physicalAttributes.fieldMetadata[field] = FieldMetadata(
        prisonerNumber = prisonerNumber,
        field = field,
        lastModifiedAt = latestRecord.createdAt,
        lastModifiedBy = latestRecord.createdBy,
      )
    }

    migration.forEach { it.addToHistory(physicalAttributes, now) }

    return physicalAttributesRepository.save(physicalAttributes).fieldHistory
      .map { it.fieldHistoryId }
      .also { trackMigrationEvent(prisonerNumber, it) }
      .let { PhysicalAttributesMigrationResponse(it) }
  }

  private fun PhysicalAttributesMigrationRequest.addToHistory(physicalAttributes: PhysicalAttributes, now: ZonedDateTime) {
    fieldsToMigrate().onEach { (field, value) ->
      physicalAttributes.fieldHistory.add(
        FieldHistory(
          prisonerNumber = physicalAttributes.prisonerNumber,
          field = field,
          appliesFrom = appliesFrom,
          appliesTo = appliesTo,
          createdAt = createdAt,
          createdBy = createdBy,
          source = NOMIS,
          migratedAt = now,
        ).also { field.set(it, value()) },
      )
    }
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
  }
}
