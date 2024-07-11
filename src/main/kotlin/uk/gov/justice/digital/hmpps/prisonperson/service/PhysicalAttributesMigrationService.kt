package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
  private val clock: Clock,
) {
  fun migrate(
    prisonerNumber: String,
    migration: SortedSet<PhysicalAttributesMigrationRequest>,
  ): PhysicalAttributesMigrationResponse {
    val now = ZonedDateTime.now(clock)
    val latestRecord = migration.last()

    val physicalAttributes = PhysicalAttributes(
      prisonerNumber,
      height = latestRecord.height,
      weight = latestRecord.weight,
    )

    listOf(HEIGHT, WEIGHT).onEach { field ->
      physicalAttributes.fieldMetadata[field] = FieldMetadata(
        prisonerNumber = prisonerNumber,
        field = field,
        lastModifiedAt = latestRecord.createdAt,
        lastModifiedBy = latestRecord.createdBy,
      )
    }

    migration.forEach { it.addToHistory(physicalAttributes, now) }

    return PhysicalAttributesMigrationResponse(
      physicalAttributesRepository.save(physicalAttributes).fieldHistory.map { it.fieldHistoryId },
    )
  }

  private fun PhysicalAttributesMigrationRequest.addToHistory(physicalAttributes: PhysicalAttributes, now: ZonedDateTime) {
    val fieldsToMigrate = mapOf(
      HEIGHT to ::height,
      WEIGHT to ::weight,
    )

    fieldsToMigrate.onEach { (field, getter) ->
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
        ).also { field.set(it, getter()) },
      )
    }
  }
}
