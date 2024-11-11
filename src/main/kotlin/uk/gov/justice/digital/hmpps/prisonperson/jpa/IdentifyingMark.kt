package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.CascadeType.MERGE
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.EAGER
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.IdentifyingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.IdentifyingMarkImageDto
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.utils.GeneratedUuidV7
import java.time.ZonedDateTime
import java.util.UUID

@Entity
class IdentifyingMark(
  @Id
  @GeneratedUuidV7
  @Column(name = "identifying_mark_id", updatable = false, nullable = false)
  val identifyingMarkId: UUID? = null,

  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "body_part_code", referencedColumnName = "id", nullable = false)
  var bodyPart: ReferenceDataCode,

  @ManyToOne
  @JoinColumn(name = "mark_type", referencedColumnName = "id", nullable = false)
  var markType: ReferenceDataCode,

  @ManyToOne
  @JoinColumn(name = "side_code", referencedColumnName = "id")
  var side: ReferenceDataCode? = null,

  @ManyToOne
  @JoinColumn(name = "part_orientation", referencedColumnName = "id")
  var partOrientation: ReferenceDataCode? = null,

  @Column(name = "comment_text")
  var comment: String? = null,

  @OneToMany(mappedBy = "identifyingMark", fetch = EAGER, cascade = [PERSIST, MERGE])
  var photographUuids: MutableSet<IdentifyingMarkImage> = mutableSetOf(),

  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
) {
  fun toDto(): IdentifyingMarkDto = IdentifyingMarkDto(
    id = identifyingMarkId.toString(),
    prisonerNumber,
    bodyPart.toSimpleDto(),
    markType.toSimpleDto(),
    side?.toSimpleDto(),
    partOrientation?.toSimpleDto(),
    comment,
    photographUuids.map { it.toDto() },
    createdAt,
    createdBy,
  )

  fun addNewImage(uuid: String): IdentifyingMarkImage {
    val identifyingMarkImage = IdentifyingMarkImage(
      identifyingMarkImageId = UUID.fromString(uuid),
      identifyingMark = this,
      latest = true,
    )

    photographUuids.find { it.latest }?.latest = false
    photographUuids.add(identifyingMarkImage)
    return identifyingMarkImage
  }
}

@Entity
class IdentifyingMarkImage(
  @Id
  @Column(name = "identifying_mark_image_id", updatable = false, nullable = false)
  val identifyingMarkImageId: UUID,

  @ManyToOne
  @JoinColumn(name = "identifying_mark_id", referencedColumnName = "identifying_mark_id")
  val identifyingMark: IdentifyingMark,

  var latest: Boolean = false,
) {
  fun toDto(): IdentifyingMarkImageDto = IdentifyingMarkImageDto(
    id = identifyingMarkImageId.toString(),
    latest = latest,
  )
}
