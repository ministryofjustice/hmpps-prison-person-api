package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.ZonedDateTime

@Entity
class PhysicalAttributesHistory(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val physicalAttributesHistoryId: Long = -1,

  @ManyToOne
  @JoinColumn(name = "prisoner_number")
  val physicalAttributes: PhysicalAttributes,

  @Column(name = "height_cm")
  var height: Int? = null,

  @Column(name = "weight_kg")
  var weight: Int? = null,

  val migratedAt: ZonedDateTime? = null,
  val appliesFrom: ZonedDateTime = ZonedDateTime.now(),
  val appliesTo: ZonedDateTime? = null,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String? = null,
)
