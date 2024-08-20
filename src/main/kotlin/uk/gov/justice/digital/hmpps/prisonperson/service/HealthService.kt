package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.HealthUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.Health
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.HealthRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.utils.validatePrisonerNumber
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class HealthService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val healthRepository: HealthRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) {
  fun getHealth(prisonerNumber: String): HealthDto? = healthRepository.findById(prisonerNumber).getOrNull()?.toDto()

  fun createOrUpdate(
    prisonerNumber: String,
    request: HealthUpdateRequest,
  ): HealthDto {
    val health = healthRepository.findById(prisonerNumber).orElseGet { newHealthFor(prisonerNumber) }.apply {
      request.smokerOrVaper.apply(
        this::smokerOrVaper,
        { toReferenceDataCode(referenceDataCodeRepository, it) },
      )
    }

    return healthRepository.save(health).toDto()
  }

  private fun newHealthFor(prisonerNumber: String): Health {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return Health(prisonerNumber)
  }
}
