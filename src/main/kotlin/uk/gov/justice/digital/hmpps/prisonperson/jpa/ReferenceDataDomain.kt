package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
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
) {
  var lastModifiedAt: ZonedDateTime? = null
  var lastModifiedBy: String? = null
  var deactivatedAt: ZonedDateTime? = null
  var deactivatedBy: String? = null
  var migratedAt: ZonedDateTime? = null

  @OneToMany(mappedBy = "domain")
  var referenceDataCodes: MutableList<ReferenceDataCode> = mutableListOf()
}
