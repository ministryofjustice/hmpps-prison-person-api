package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.springframework.data.domain.AbstractAggregateRoot
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
) : AbstractAggregateRoot<ReferenceDataCode>() {
  var lastModifiedAt: ZonedDateTime? = null
  var lastModifiedBy: String? = null
  var deactivatedAt: ZonedDateTime? = null
  var deactivatedBy: String? = null
  var migratedAt: ZonedDateTime? = null


  fun ReferenceDataCode.toDto() = ReferenceDataCodeDto(
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

  fun Collection<ReferenceDataCode>.toDtos() = map { it.toDto() }

  fun isActive() = deactivatedAt?.isBefore(ZonedDateTime.now()) != true

}
