package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributesHistory

@Repository
interface PhysicalAttributesHistoryRepository : JpaRepository<PhysicalAttributesHistory, Long>
