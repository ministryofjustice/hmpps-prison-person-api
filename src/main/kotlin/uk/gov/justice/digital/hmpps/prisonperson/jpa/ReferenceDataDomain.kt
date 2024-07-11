package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.springframework.data.domain.AbstractAggregateRoot
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataDomainDto
import java.time.ZonedDateTime

@Entity
class ReferenceDataDomain(
  @Id
  @Column(name = "code", updatable = false, nullable = false)
  val code: String,

  var description: String,
  var listSequence: Int,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
) : AbstractAggregateRoot<ReferenceDataDomain>() {
  var lastModifiedAt: ZonedDateTime? = null
  var lastModifiedBy: String? = null
  var deactivatedAt: ZonedDateTime? = null
  var deactivatedBy: String? = null
  var migratedAt: ZonedDateTime? = null

  @OneToMany(mappedBy = "domain")
  val referenceDataCodes: MutableList<ReferenceDataCode> = mutableListOf()

  fun toDto(): ReferenceDataDomainDto = ReferenceDataDomainDto(
    code,
    description,
    listSequence,
    isActive(),
    createdAt,
    createdBy,
    lastModifiedAt,
    lastModifiedBy,
    deactivatedAt,
    deactivatedBy,
    referenceDataCodes,
  )

  fun Collection<ReferenceDataDomain>.toDtos() = map { it.toDto() }

  fun isActive() = deactivatedAt?.isBefore(ZonedDateTime.now()) != true

}
