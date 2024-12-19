package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMarkHistory
import java.util.*

@Repository
interface DistinguishingMarkHistoryRepository : JpaRepository<DistinguishingMarkHistory, Long> {
  fun findAllByMarkDistinguishingMarkId(markDistinguishingMarkId: UUID): MutableList<DistinguishingMarkHistory>
}