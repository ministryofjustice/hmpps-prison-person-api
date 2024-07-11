package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import java.time.ZonedDateTime

@Entity
class ReferenceDataCode(
  @Id
  @Column(name = "code", updatable = false, nullable = false)
  val code: String,

  @ManyToOne
  val domain: ReferenceDataDomain,

  var description: String,
  var listSequence: Int,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
) {
  var lastModifiedAt: ZonedDateTime? = null
  var lastModifiedBy: String? = null
  var deactivatedAt: ZonedDateTime? = null
  var deactivatedBy: String? = null
  var migratedAt: ZonedDateTime? = null

  fun toDto(): ReferenceDataCodeDto = ReferenceDataCodeDto(
    domain.code,
    code,
    description,
    listSequence,
    isActive(),
    createdAt,
    createdBy,
    lastModifiedAt,
    deactivatedBy,
    deactivatedAt,
    deactivatedBy,
  )

  fun isActive() = deactivatedAt?.isBefore(ZonedDateTime.now()) != true
}
