package uk.gov.justice.digital.hmpps.prisonperson.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.ProfileDetailsPhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.SyncValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ProfileDetailsPhysicalAttributesSyncResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL
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

    val physicalAttributes = physicalAttributesRepository.findByIdForUpdate(prisonerNumber)
      .orElseGet {
        physicalAttributesService.ensurePhysicalAttributesPersistedFor(prisonerNumber)
        physicalAttributesRepository.findByIdForUpdate(prisonerNumber).orElseThrow()
      }
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
      }

    val changedFields = listOf(
      request.hair to HAIR,
      request.facialHair to FACIAL_HAIR,
      request.face to FACE,
      request.build to BUILD,
      request.leftEyeColour to LEFT_EYE_COLOUR,
      request.rightEyeColour to RIGHT_EYE_COLOUR,
      request.shoeSize to SHOE_SIZE,
    )
      .filter { (attr) -> attr != null }
      .flatMap { (attr, field) -> updateFieldHistory(attr!!, field, physicalAttributes) }

    return physicalAttributesRepository.save(physicalAttributes).getLatestFieldHistoryIds(updatedFields)
      .also { physicalAttributes.publishUpdateEvent(PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED, NOMIS, now, changedFields, it) }
      .also { physicalAttributesRepository.save(physicalAttributes) } // save() required after publishEvent
      .let { ProfileDetailsPhysicalAttributesSyncResponse(it) }
  }

  private fun syncHistoricalProfileDetailsPhysicalAttributes(
    prisonerNumber: String,
    request: ProfileDetailsPhysicalAttributesSyncRequest,
  ): ProfileDetailsPhysicalAttributesSyncResponse {
    log.debug("Syncing historical profile details physical attributes update from NOMIS for prisoner: $prisonerNumber")

    val now = ZonedDateTime.now(clock)
    val updatedFields = mutableSetOf<PrisonPersonField>()

    val physicalAttributes = physicalAttributesRepository.findByIdForUpdate(prisonerNumber)
      .orElseGet {
        physicalAttributesService.ensurePhysicalAttributesPersistedFor(prisonerNumber)
        physicalAttributesRepository.findByIdForUpdate(prisonerNumber).orElseThrow()
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
          }
      }

    return request.addToHistory(prisonerNumber)
      .map { it.fieldHistoryId }
      .also { physicalAttributes.publishUpdateEvent(PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL, NOMIS, now, emptyList(), it) }
      .also { physicalAttributesRepository.save(physicalAttributes) } // save() required after publishEvent
      .let { ProfileDetailsPhysicalAttributesSyncResponse(it) }
  }

  private fun updateFieldHistory(
    attribute: SyncValueWithMetadata<String>,
    field: PrisonPersonField,
    physicalAttributes: PhysicalAttributes,
  ): Collection<PrisonPersonField> =
    physicalAttributes.updateFieldHistory(
      lastModifiedAt = attribute.lastModifiedAt,
      lastModifiedBy = attribute.lastModifiedBy,
      source = NOMIS,
      fields = listOf(field),
    )

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
              field.domain?.let { domain ->
                toReferenceDataCode(referenceDataCodeRepository, toReferenceDataCodeId(getter()!!.value, domain))
              } ?: getter()!!.value,
            )
          },
        )
      }
  }

  private fun PhysicalAttributes.getLatestFieldHistoryIds(fields: Set<PrisonPersonField>) =
    fields
      .mapNotNull { field -> fieldHistory.lastOrNull { it.field == field } }
      .map { it.fieldHistoryId }

  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
