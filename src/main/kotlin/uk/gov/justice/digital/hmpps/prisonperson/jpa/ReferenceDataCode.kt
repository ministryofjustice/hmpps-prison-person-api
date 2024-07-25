package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.ZonedDateTime

@Entity
class ReferenceDataCode(
  @Id
  @Column(name = "id", updatable = false, nullable = false)
  val id: String,

  @Column(name = "code", updatable = false, nullable = false)
  val code: String,

  @ManyToOne
  @JoinColumn(name = "domain", nullable = false)
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
}
