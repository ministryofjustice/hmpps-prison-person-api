package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributesHistory
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
  ): PhysicalAttributesDto {
    val now = ZonedDateTime.now(clock)
    val latestRecord = migration.last()
    val oldestCreatedRecord = migration.minBy { it.createdAt }

    val physicalAttributes = PhysicalAttributes(
      prisonerNumber,
      height = latestRecord.height,
      weight = latestRecord.weight,
      createdAt = oldestCreatedRecord.createdAt,
      createdBy = oldestCreatedRecord.createdBy,
      lastModifiedAt = latestRecord.createdAt,
      lastModifiedBy = latestRecord.createdBy,
      migratedAt = now,
    ).also { it.createNewFieldMetadata() }

    migration.forEach { it.addToHistory(physicalAttributes, now) }

    return physicalAttributesRepository.save(physicalAttributes).toDto()
  }

  private fun PhysicalAttributesMigrationRequest.addToHistory(physicalAttributes: PhysicalAttributes, now: ZonedDateTime) {
    physicalAttributes.history.add(
      PhysicalAttributesHistory(
        physicalAttributes = physicalAttributes,
        height = height,
        weight = weight,
        appliesFrom = appliesFrom,
        appliesTo = appliesTo,
        createdAt = createdAt,
        createdBy = createdBy,
        migratedAt = now,
      ),
    )
  }
}
