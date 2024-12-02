package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMarkHistory

@Repository
interface DistinguishingMarkHistoryRepository : JpaRepository<DistinguishingMarkHistory, Long>