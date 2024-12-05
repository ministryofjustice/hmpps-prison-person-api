package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCodeWithDefault
import uk.gov.justice.digital.hmpps.prisonperson.utils.validatePrisonerNumber
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class PhysicalAttributesService(
  private val physicalAttributesRepository: PhysicalAttributesRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val authenticationFacade: AuthenticationFacade,
  private val clock: Clock,
) {
  fun getPhysicalAttributes(prisonNumber: String): PhysicalAttributesDto? =
    physicalAttributesRepository.findById(prisonNumber).getOrNull()?.toDto()

  @Transactional
  fun createOrUpdate(
    prisonerNumber: String,
    request: PhysicalAttributesUpdateRequest,
  ): PhysicalAttributesDto {
    val now = ZonedDateTime.now(clock)

    val physicalAttributes = physicalAttributesRepository.findById(prisonerNumber)
      .orElseGet { newPhysicalAttributesFor(prisonerNumber) }
      .apply {
        height = request.height.orElse(this.height)
        weight = request.weight.orElse(this.weight)
        hair = toReferenceDataCodeWithDefault(referenceDataCodeRepository, request.hair, this.hair)
        facialHair = toReferenceDataCodeWithDefault(referenceDataCodeRepository, request.facialHair, this.facialHair)
        face = toReferenceDataCodeWithDefault(referenceDataCodeRepository, request.face, this.face)
        build = toReferenceDataCodeWithDefault(referenceDataCodeRepository, request.build, this.build)
        leftEyeColour = toReferenceDataCodeWithDefault(referenceDataCodeRepository, request.leftEyeColour, this.leftEyeColour)
        rightEyeColour = toReferenceDataCodeWithDefault(referenceDataCodeRepository, request.rightEyeColour, this.rightEyeColour)
        shoeSize = request.shoeSize.orElse(this.shoeSize)
      }
      .also {
        val changedFields = it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext())
        it.publishUpdateEvent(PHYSICAL_ATTRIBUTES_UPDATED, DPS, now, changedFields)
      }

    return physicalAttributesRepository.save(physicalAttributes).toDto()
  }

  fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return PhysicalAttributes(prisonerNumber)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun ensurePhysicalAttributesPersistedFor(prisonerNumber: String) {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    physicalAttributesRepository.newPhysicalAttributesFor(prisonerNumber)
  }
}
