package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributesHistory
import java.util.SortedSet

@Repository
interface PhysicalAttributesHistoryRepository : JpaRepository<PhysicalAttributesHistory, Long> {
  fun findAllByPhysicalAttributesPrisonerNumber(prisonerNumber: String): SortedSet<PhysicalAttributesHistory>
}
