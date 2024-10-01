package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCode
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
        request.height.apply(::height)
        request.weight.apply(::weight)
        request.hair.apply(::hair, { toReferenceDataCode(referenceDataCodeRepository, it) })
        request.facialHair.apply(::facialHair, { toReferenceDataCode(referenceDataCodeRepository, it) })
        request.face.apply(::face, { toReferenceDataCode(referenceDataCodeRepository, it) })
        request.build.apply(::build, { toReferenceDataCode(referenceDataCodeRepository, it) })
        request.leftEyeColour.apply(::leftEyeColour, { toReferenceDataCode(referenceDataCodeRepository, it) })
        request.rightEyeColour.apply(::rightEyeColour, { toReferenceDataCode(referenceDataCodeRepository, it) })
        request.shoeSize.apply(::shoeSize)
      }
      .also {
        val changedFields = it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext())
        it.publishUpdateEvent(DPS, now, changedFields)
      }

    return physicalAttributesRepository.save(physicalAttributes).toDto()
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun newPhysicalAttributesFor(prisonerNumber: String): PhysicalAttributes {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return physicalAttributesRepository.save(PhysicalAttributes(prisonerNumber))
  }
}
