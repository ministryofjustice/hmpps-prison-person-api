package uk.gov.justice.digital.hmpps.prisonperson.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.trackEvent
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.ProfileDetailsPhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.SyncValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ProfileDetailsPhysicalAttributesSyncResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCodeId
import java.time.Clock
import java.time.ZonedDateTime

@Service
@Transactional
class ProfileDetailsPhysicalAttributesSyncService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val fieldHistoryRepository: FieldHistoryRepository,
  private val physicalAttributesService: PhysicalAttributesService,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
  private val telemetryClient: TelemetryClient,
  private val clock: Clock,
) {
  fun sync(
    prisonerNumber: String,
    request: ProfileDetailsPhysicalAttributesSyncRequest,
  ): ProfileDetailsPhysicalAttributesSyncResponse = if (request.latestBooking == true) {
    syncLatestProfileDetailsPhysicalAttributes(prisonerNumber, request)
  } else {
    syncHistoricalProfileDetailsPhysicalAttributes(prisonerNumber, request)
  }

  private fun syncLatestProfileDetailsPhysicalAttributes(
    prisonerNumber: String,
    request: ProfileDetailsPhysicalAttributesSyncRequest,
  ): ProfileDetailsPhysicalAttributesSyncResponse {
    log.debug("Syncing latest profile details physical attributes update from NOMIS for prisoner: $prisonerNumber")

    val now = ZonedDateTime.now(clock)
    val updatedFields = mutableSetOf<PrisonPersonField>()

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { physicalAttributesService.newPhysicalAttributesFor(prisonerNumber) }
      .apply {
        request.hair?.let { hair = updateField(it, referenceDataCodeRepository, HAIR, updatedFields) }
        request.facialHair?.let {
          facialHair = updateField(it, referenceDataCodeRepository, FACIAL_HAIR, updatedFields)
        }
        request.face?.let { face = updateField(it, referenceDataCodeRepository, FACE, updatedFields) }
        request.build?.let { build = updateField(it, referenceDataCodeRepository, BUILD, updatedFields) }
        request.leftEyeColour?.let {
          leftEyeColour = updateField(it, referenceDataCodeRepository, LEFT_EYE_COLOUR, updatedFields)
        }
        request.rightEyeColour?.let {
          rightEyeColour = updateField(it, referenceDataCodeRepository, RIGHT_EYE_COLOUR, updatedFields)
        }
        request.shoeSize?.let {
          updatedFields.add(SHOE_SIZE)
          shoeSize = it.value
        }
      }.also { attributes ->
        listOf(
          request.hair to HAIR,
          request.facialHair to FACIAL_HAIR,
          request.face to FACE,
          request.build to BUILD,
          request.leftEyeColour to LEFT_EYE_COLOUR,
          request.rightEyeColour to RIGHT_EYE_COLOUR,
          request.shoeSize to SHOE_SIZE,
        ).forEach { (attr, field) ->
          updateFieldHistory(attr, request, field, attributes)
        }
      }
      .also { it.publishUpdateEvent(NOMIS, now) }

    return physicalAttributesRepository.save(physicalAttributes).getLatestFieldHistoryIds(updatedFields)
      .also { trackSyncEvent(prisonerNumber, it) }
      .let { ProfileDetailsPhysicalAttributesSyncResponse(it) }
  }

  private fun syncHistoricalProfileDetailsPhysicalAttributes(
    prisonerNumber: String,
    request: ProfileDetailsPhysicalAttributesSyncRequest,
  ): ProfileDetailsPhysicalAttributesSyncResponse {
    log.debug("Syncing historical profile details physical attributes update from NOMIS for prisoner: $prisonerNumber")

    physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet {
        physicalAttributesRepository.save(
          physicalAttributesService.newPhysicalAttributesFor(prisonerNumber).apply {
            hair = toReferenceDataCode(
              referenceDataCodeRepository,
              toReferenceDataCodeId(request.hair?.value, "HAIR"),
            )
            facialHair = toReferenceDataCode(
              referenceDataCodeRepository,
              toReferenceDataCodeId(request.facialHair?.value, "FACIAL_HAIR"),
            )
            face = toReferenceDataCode(
              referenceDataCodeRepository,
              toReferenceDataCodeId(request.face?.value, "FACE"),
            )
            build =
              toReferenceDataCode(
                referenceDataCodeRepository,
                toReferenceDataCodeId(request.build?.value, "BUILD"),
              )
            leftEyeColour = toReferenceDataCode(
              referenceDataCodeRepository,
              toReferenceDataCodeId(request.leftEyeColour?.value, "EYE"),
            )
            rightEyeColour = toReferenceDataCode(
              referenceDataCodeRepository,
              toReferenceDataCodeId(request.rightEyeColour?.value, "EYE"),
            )
            shoeSize = request.shoeSize?.value
          },
        )
      }
    return request.addToHistory(prisonerNumber)
      .map { it.fieldHistoryId }
      .also { trackSyncEvent(prisonerNumber, it) }
      .let { ProfileDetailsPhysicalAttributesSyncResponse(it) }
  }

  private fun updateFieldHistory(
    attribute: SyncValueWithMetadata<String>?,
    record: ProfileDetailsPhysicalAttributesSyncRequest,
    field: PrisonPersonField,
    physicalAttributes: PhysicalAttributes,
  ) {
    attribute?.let {
      physicalAttributes.updateFieldHistory(
        appliesFrom = record.appliesFrom,
        appliesTo = record.appliesTo,
        lastModifiedAt = it.lastModifiedAt,
        lastModifiedBy = it.lastModifiedBy,
        source = NOMIS,
        fields = listOf(field),
      )
    }
  }

  private fun updateField(
    requestField: SyncValueWithMetadata<String>,
    referenceDataCodeRepository: ReferenceDataCodeRepository,
    field: PrisonPersonField,
    updatedFields: MutableSet<PrisonPersonField>,
  ): ReferenceDataCode? {
    updatedFields.add(field)
    return toReferenceDataCode(referenceDataCodeRepository, toReferenceDataCodeId(requestField.value, field.domain))
  }

  private fun ProfileDetailsPhysicalAttributesSyncRequest.addToHistory(prisonerNumber: String): List<FieldHistory> {
    val fieldsToSync = mapOf(
      HAIR to ::hair,
      FACIAL_HAIR to ::facialHair,
      FACE to ::face,
      BUILD to ::build,
      LEFT_EYE_COLOUR to ::leftEyeColour,
      RIGHT_EYE_COLOUR to ::rightEyeColour,
      SHOE_SIZE to ::shoeSize,
    )

    return fieldsToSync
      .filter { (_, getter) -> getter() != null }
      .map { (field, getter) ->
        fieldHistoryRepository.save(
          FieldHistory(
            prisonerNumber = prisonerNumber,
            field = field,
            appliesFrom = appliesFrom,
            appliesTo = appliesTo,
            createdAt = getter()!!.lastModifiedAt,
            createdBy = getter()!!.lastModifiedBy,
            source = NOMIS,
          ).also {
            field.set(
              it,
              toReferenceDataCode(
                referenceDataCodeRepository,
                toReferenceDataCodeId(getter()!!.value, field.domain),
              ),
            )
          },
        )
      }
  }

  private fun PhysicalAttributes.getLatestFieldHistoryIds(fields: Set<PrisonPersonField>) =
    fields
      .mapNotNull { field -> fieldHistory.lastOrNull { it.field == field } }
      .map { it.fieldHistoryId }

  private fun trackSyncEvent(prisonerNumber: String, fieldHistoryIds: List<Long>) {
    telemetryClient.trackEvent(
      "prison-person-api-profile-details-physical-attributes-synced",
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