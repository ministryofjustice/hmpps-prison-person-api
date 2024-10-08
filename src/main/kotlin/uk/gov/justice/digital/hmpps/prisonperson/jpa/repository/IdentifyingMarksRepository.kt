package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.IdentifyingMark
import java.util.UUID

@Repository
interface IdentifyingMarksRepository : JpaRepository<IdentifyingMark, UUID> {
  fun findAllByPrisonerNumber(prisonerNumber: String): List<IdentifyingMark>
}
