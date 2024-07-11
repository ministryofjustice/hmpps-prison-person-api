package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain

@Repository
interface ReferenceDataDomainRepository : JpaRepository<ReferenceDataDomain, String> {

  @Query(
    """
        SELECT rdd 
        FROM ReferenceDataDomain rdd 
        WHERE :includeInactive = true OR 
              (:includeInactive = false AND rdd.deactivatedAt IS NULL)
        ORDER BY rdd.listSequence, rdd.description 
    """,
  )
  fun findAllByIncludeInactive(@Param("includeInactive") includeInactive: Boolean): Collection<ReferenceDataDomain>
}
