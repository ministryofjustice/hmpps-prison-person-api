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
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkImageDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.DistinguishingMarkImageRepository
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.utils.GeneratedUuidV7
import java.time.ZonedDateTime
import java.util.UUID

@Entity
class DistinguishingMark(
  @Id
  @GeneratedUuidV7
  @Column(name = "distinguishing_mark_id", updatable = false, nullable = false)
  val distinguishingMarkId: UUID? = null,

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

  @OneToMany(mappedBy = "distinguishingMark", fetch = EAGER, cascade = [PERSIST, MERGE])
  var photographUuids: MutableSet<DistinguishingMarkImage> = mutableSetOf(),

  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
) {
  fun toDto(): DistinguishingMarkDto = DistinguishingMarkDto(
    id = distinguishingMarkId.toString(),
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

  fun addNewImage(
    uuid: String,
    distinguishingMarkImageRepository: DistinguishingMarkImageRepository,
  ): DistinguishingMarkImage {
    val distinguishingMarkImage = DistinguishingMarkImage(
      distinguishingMarkImageId = UUID.fromString(uuid),
      distinguishingMark = this,
      latest = true,
    )

    photographUuids
      .find { it.latest }
      ?.apply { latest = false }
      ?.let { distinguishingMarkImageRepository.saveAndFlush(it) }

    photographUuids.add(distinguishingMarkImage)
    return distinguishingMarkImage
  }
}

@Entity
class DistinguishingMarkImage(
  @Id
  @Column(name = "distinguishing_mark_image_id", updatable = false, nullable = false)
  val distinguishingMarkImageId: UUID,

  @ManyToOne
  @JoinColumn(name = "distinguishing_mark_id", referencedColumnName = "distinguishing_mark_id")
  val distinguishingMark: DistinguishingMark,

  var latest: Boolean = false,
) {
  fun toDto(): DistinguishingMarkImageDto = DistinguishingMarkImageDto(
    id = distinguishingMarkImageId.toString(),
    latest = latest,
  )
}
