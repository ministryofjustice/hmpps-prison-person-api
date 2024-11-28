package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PrisonerHealthUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.prisonperson.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCodeWithDefault
import uk.gov.justice.digital.hmpps.prisonperson.utils.validatePrisonerNumber
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class PrisonerHealthService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerHealthRepository: PrisonerHealthRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val clock: Clock,
) {
  fun getHealth(prisonerNumber: String): HealthDto? =
    prisonerHealthRepository.findById(prisonerNumber).getOrNull()?.toDto()

  @Transactional
  fun createOrUpdate(
    prisonerNumber: String,
    request: PrisonerHealthUpdateRequest,
  ): HealthDto {
    val now = ZonedDateTime.now(clock)
    val health = prisonerHealthRepository.findById(prisonerNumber).orElseGet { newHealthFor(prisonerNumber) }.apply {
      smokerOrVaper = toReferenceDataCodeWithDefault(referenceDataCodeRepository, request.smokerOrVaper, this.smokerOrVaper)

      request.foodAllergies.ifPresent {
        foodAllergies.apply { clear() }.addAll(
          it!!.map { allergyCode ->
            FoodAllergy(
              prisonerNumber = prisonerNumber,
              allergy = toReferenceDataCode(referenceDataCodeRepository, allergyCode)!!,
            )
          },
        )
      }

      request.medicalDietaryRequirements.ifPresent {
        medicalDietaryRequirements.apply { clear() }.addAll(
          it!!.map { dietaryCode ->
            MedicalDietaryRequirement(
              prisonerNumber = prisonerNumber,
              dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, dietaryCode)!!,
            )
          },
        )
      }
    }.also { it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext()) }

    return prisonerHealthRepository.save(health).toDto()
  }

  private fun newHealthFor(prisonerNumber: String): PrisonerHealth {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return PrisonerHealth(prisonerNumber)
  }
}
