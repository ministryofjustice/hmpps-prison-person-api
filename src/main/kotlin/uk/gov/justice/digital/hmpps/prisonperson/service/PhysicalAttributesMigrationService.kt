package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesMigrationRequest

@Service
@Transactional
class PhysicalAttributesMigrationService {
  fun migrate(
    prisonerNumber: String,
    migration: List<PhysicalAttributesMigrationRequest>,
  ): PhysicalAttributesDto {
    TODO("CDPS-789")
  }
}
