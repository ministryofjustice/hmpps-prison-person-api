package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import java.util.*

@Repository
interface ReferenceDataCodeRepository : JpaRepository<ReferenceDataCode, String> {

  @Query(
    """
        SELECT rdc
        FROM ReferenceDataCode rdc 
        WHERE :domain = rdc.code AND
              :includeInactive = true OR 
              (:includeInactive = false AND rdc.deactivatedAt IS NULL)
        ORDER BY rdc.listSequence, rdc.description
    """,
  )
  fun findAllByDomainAndIncludeInactive(
    @Param("domain") domain: String,
    @Param("includeInactive") includeInactive: Boolean,
  ): Collection<ReferenceDataCode>

  @Query("SELECT rdc FROM ReferenceDataCode rdc WHERE rdc.code = :code AND rdc.domain.code = :domain")
  fun findByIdAndDomain(@Param("code") code: String, @Param("domain") domain: String): Optional<ReferenceDataCode>
}
