package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata

@Repository
interface FieldMetadataRepository : JpaRepository<FieldMetadata, String> {
  fun findAllByPrisonerNumber(prisonerNumber: String): List<FieldMetadata>
  fun deleteAllByPrisonerNumber(prisonerNumber: String)
}
