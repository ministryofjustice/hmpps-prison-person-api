package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.time.ZonedDateTime

@Entity
@IdClass(ReferenceDataCodeId::class)
class ReferenceDataCode(

  @Id
  @Column(updatable = false, nullable = false)
  val code: String,

  @Id
  @ManyToOne
  @JoinColumn(name = "domain", referencedColumnName = "code", nullable = false)
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

data class ReferenceDataCodeId(
  val code: String = "",
  val domain: String = "",
) : Serializable
