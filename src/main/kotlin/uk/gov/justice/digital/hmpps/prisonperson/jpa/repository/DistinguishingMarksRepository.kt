package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMark
import java.util.UUID

@Repository
interface DistinguishingMarksRepository : JpaRepository<DistinguishingMark, UUID> {
  fun findAllByPrisonerNumber(prisonerNumber: String): List<DistinguishingMark>
}
