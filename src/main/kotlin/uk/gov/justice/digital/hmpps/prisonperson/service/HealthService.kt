package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.HealthRepository
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class HealthService(
  private val healthRepository: HealthRepository,
) {
  fun getHealth(prisonerNumber: String): HealthDto? = healthRepository.findById(prisonerNumber).getOrNull()?.toDto()
}
