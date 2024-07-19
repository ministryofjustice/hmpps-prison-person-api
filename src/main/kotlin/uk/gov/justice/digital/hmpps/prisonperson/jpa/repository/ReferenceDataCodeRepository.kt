package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCodeId

@Repository
interface ReferenceDataCodeRepository : JpaRepository<ReferenceDataCode, ReferenceDataCodeId> {

  @Query(
    """
        SELECT rdc
        FROM ReferenceDataCode rdc 
        WHERE :domain = rdc.domain.code AND
              (:includeInactive = true OR 
              (:includeInactive = false AND rdc.deactivatedAt IS NULL))
        ORDER BY CASE 
                    WHEN rdc.listSequence = 0 THEN 999 
                    ELSE rdc.listSequence
                 END,
                 rdc.description
    """,
  )
  fun findAllByDomainCodeAndIncludeInactive(
    @Param("domain") domain: String,
    @Param("includeInactive") includeInactive: Boolean,
  ): Collection<ReferenceDataCode>

  fun findByCodeAndDomainCode(code: String, domain: String): ReferenceDataCode?
}
