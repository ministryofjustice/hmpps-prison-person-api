package uk.gov.justice.digital.hmpps.prisonperson.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.trackEvent
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.MigrationValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.ProfileDetailsPhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ProfileDetailsPhysicalAttributesMigrationResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCode
import java.time.Clock
import java.time.ZonedDateTime
import java.util.SortedSet

@Service
@Transactional
class ProfileDetailsPhysicalAttributesMigrationService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
  private val telemetryClient: TelemetryClient,
  private val clock: Clock,
) {
  fun migrate(
    prisonerNumber: String,
    migration: SortedSet<ProfileDetailsPhysicalAttributesMigrationRequest>,
  ): ProfileDetailsPhysicalAttributesMigrationResponse {
    log.info("Attempting to migrate profile details physical attributes for $prisonerNumber")

    if (migration.isEmpty()) {
      trackMigrationEvent(prisonerNumber, listOf())
      return ProfileDetailsPhysicalAttributesMigrationResponse()
    }

    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { newPhysicalAttributesFor(prisonerNumber) }
      .also { it.resetHistoryForMigratedFields() }

    migration.forEach { record ->
      physicalAttributes.apply {
        hair = toReferenceDataCode(referenceDataCodeRepository, record.hair?.value)
        facialHair = toReferenceDataCode(referenceDataCodeRepository, record.facialHair?.value)
        face = toReferenceDataCode(referenceDataCodeRepository, record.face?.value)
        build = toReferenceDataCode(referenceDataCodeRepository, record.build?.value)
        leftEyeColour = toReferenceDataCode(referenceDataCodeRepository, record.leftEyeColour?.value)
        rightEyeColour = toReferenceDataCode(referenceDataCodeRepository, record.rightEyeColour?.value)
        shoeSize = record.shoeSize?.value
      }.also {
        updateFieldHistory(record.hair, record, HAIR, it, now)
        updateFieldHistory(record.facialHair, record, FACIAL_HAIR, it, now)
        updateFieldHistory(record.face, record, FACE, it, now)
        updateFieldHistory(record.build, record, BUILD, it, now)
        updateFieldHistory(record.leftEyeColour, record, LEFT_EYE_COLOUR, it, now)
        updateFieldHistory(record.rightEyeColour, record, RIGHT_EYE_COLOUR, it, now)
        updateFieldHistory(record.shoeSize, record, SHOE_SIZE, it, now)
      }
    }

    return physicalAttributesRepository.save(physicalAttributes).fieldHistory
      .filter { it.migratedAt == now }
      .map { it.fieldHistoryId }
      .also { trackMigrationEvent(prisonerNumber, it) }
      .let { ProfileDetailsPhysicalAttributesMigrationResponse(it) }
  }

  private fun updateFieldHistory(
    attribute: MigrationValueWithMetadata<String>?,
    record: ProfileDetailsPhysicalAttributesMigrationRequest,
    field: PrisonPersonField,
    physicalAttributes: PhysicalAttributes,
    now: ZonedDateTime,
  ) {
    attribute?.let {
      physicalAttributes.updateFieldHistory(
        appliesFrom = record.appliesFrom,
        lastModifiedAt = it.lastModifiedAt,
        lastModifiedBy = it.lastModifiedBy,
        source = NOMIS,
        fields = listOf(field),
        migratedAt = now,
      )
    }
  }

  private fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes = PhysicalAttributes(prisonerNumber)

  private fun PhysicalAttributes.resetHistoryForMigratedFields() {
    fieldsToMigrate.forEach { field -> fieldHistory.removeIf { it.field == field } }
  }

  private fun trackMigrationEvent(prisonerNumber: String, fieldHistoryIds: List<Long>) {
    telemetryClient.trackEvent(
      "prison-person-api-profile-details-physical-attributes-migrated",
      mapOf(
        "prisonerNumber" to prisonerNumber,
        "fieldHistoryIds" to fieldHistoryIds.toString(),
      ),
    )
  }

  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val fieldsToMigrate = listOf(HAIR, FACIAL_HAIR, FACE, BUILD, LEFT_EYE_COLOUR, RIGHT_EYE_COLOUR, SHOE_SIZE)
  }
}
