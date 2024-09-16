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
      val appliesTo = record.appliesTo?.takeIf { record.isNestedBooking(migration) }

      physicalAttributes.apply {
        hair = toReferenceDataCode(referenceDataCodeRepository, toReferenceDataCodeId(record.hair?.value, "HAIR"))
        facialHair = toReferenceDataCode(
          referenceDataCodeRepository,
          toReferenceDataCodeId(record.facialHair?.value, "FACIAL_HAIR"),
        )
        face = toReferenceDataCode(referenceDataCodeRepository, toReferenceDataCodeId(record.face?.value, "FACE"))
        build = toReferenceDataCode(referenceDataCodeRepository, toReferenceDataCodeId(record.build?.value, "BUILD"))
        leftEyeColour =
          toReferenceDataCode(referenceDataCodeRepository, toReferenceDataCodeId(record.leftEyeColour?.value, "EYE"))
        rightEyeColour =
          toReferenceDataCode(referenceDataCodeRepository, toReferenceDataCodeId(record.rightEyeColour?.value, "EYE"))
        shoeSize = record.shoeSize?.value
      }.also {
        updateFieldHistory(record.hair, record, HAIR, it, now, appliesTo)
        updateFieldHistory(record.facialHair, record, FACIAL_HAIR, it, now, appliesTo)
        updateFieldHistory(record.face, record, FACE, it, now, appliesTo)
        updateFieldHistory(record.build, record, BUILD, it, now, appliesTo)
        updateFieldHistory(record.leftEyeColour, record, LEFT_EYE_COLOUR, it, now, appliesTo)
        updateFieldHistory(record.rightEyeColour, record, RIGHT_EYE_COLOUR, it, now, appliesTo)
        updateFieldHistory(record.shoeSize, record, SHOE_SIZE, it, now, appliesTo)
      }
    }

    return physicalAttributesRepository.save(physicalAttributes).fieldHistory
      .filter { it.migratedAt == now }
      .map { it.fieldHistoryId }
      .also { trackMigrationEvent(prisonerNumber, it) }
      .let { ProfileDetailsPhysicalAttributesMigrationResponse(it) }
  }

  private fun toReferenceDataCodeId(code: String?, domain: String) =
    code?.let { "${domain}_$code" }

  private fun updateFieldHistory(
    attribute: MigrationValueWithMetadata<String>?,
    record: ProfileDetailsPhysicalAttributesMigrationRequest,
    field: PrisonPersonField,
    physicalAttributes: PhysicalAttributes,
    now: ZonedDateTime,
    appliesTo: ZonedDateTime?,
  ) {
    attribute?.let {
      physicalAttributes.updateFieldHistory(
        appliesFrom = record.appliesFrom,
        appliesTo = appliesTo,
        lastModifiedAt = it.lastModifiedAt,
        lastModifiedBy = it.lastModifiedBy,
        source = NOMIS,
        fields = listOf(field),
        migratedAt = now,
      )
    }
  }

  /*
  This handles the case where there is an overlap in booking dates, and
  we can't define a logical boundary between one attribute being applicable with the next,
  so we explicitly set the appliesTo date.
   */
  private fun ProfileDetailsPhysicalAttributesMigrationRequest.isNestedBooking(others: SortedSet<ProfileDetailsPhysicalAttributesMigrationRequest>): Boolean =
    others
      .filterNot { it == this }
      .find { other ->
        this.appliesTo != null &&
          other.appliesFrom <= this.appliesFrom &&
          (other.appliesTo == null || other.appliesTo >= this.appliesTo)
      } != null

  private fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes =
    PhysicalAttributes(prisonerNumber)

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
