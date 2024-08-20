package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.HealthUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.Health
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.HealthRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.prisonperson.utils.PrisonerNumberUtils
import uk.gov.justice.digital.hmpps.prisonperson.utils.ReferenceCodeUtils
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class HealthService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val healthRepository: HealthRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val clock: Clock,
) {
  fun getHealth(prisonerNumber: String): HealthDto? = healthRepository.findById(prisonerNumber).getOrNull()?.toDto()

  @Transactional
  fun createOrUpdate(
    prisonerNumber: String,
    request: HealthUpdateRequest,
  ): HealthDto {
    val now = ZonedDateTime.now(clock)
    val health = healthRepository.findById(prisonerNumber)
      .orElseGet { newHealthFor(prisonerNumber) }
      .apply {
        request.smokerOrVaper.apply(
          this::smokerOrVaper,
          fun(smokerOrVaper) = ReferenceCodeUtils.toReferenceDataCode(referenceDataCodeRepository, smokerOrVaper),
        )
      }.also { it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext()) }

    return healthRepository.save(health).toDto()
  }

  private fun newHealthFor(prisonerNumber: String): Health {
    PrisonerNumberUtils.validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return Health(prisonerNumber)
  }
}
