package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import java.time.ZonedDateTime

@Entity
class PhysicalAttributes(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @Column(name = "height_cm")
  var height: Int? = null,

  @Column(name = "weight_kg")
  var weight: Int? = null,

  val migratedAt: ZonedDateTime? = null,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String? = null,
  var lastModifiedAt: ZonedDateTime = ZonedDateTime.now(),
  var lastModifiedBy: String? = null,

) {
  fun toDto(): PhysicalAttributesDto = PhysicalAttributesDto(height, weight)
}
