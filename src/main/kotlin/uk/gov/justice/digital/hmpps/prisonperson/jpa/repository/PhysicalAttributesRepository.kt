package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import java.util.Optional

@Repository
interface PhysicalAttributesRepository : JpaRepository<PhysicalAttributes, String> {
  fun deleteByPrisonerNumber(prisonerNumber: String)

  @Modifying
  @Query("INSERT INTO physical_attributes(prisoner_number) values (:id) ON CONFLICT DO NOTHING", nativeQuery = true)
  fun newPhysicalAttributesFor(id: String)

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM PhysicalAttributes p WHERE p.prisonerNumber = :id")
  fun findByIdForUpdate(id: String): Optional<PhysicalAttributes>
}
