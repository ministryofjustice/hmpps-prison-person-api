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
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.IdentifyingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.utils.GeneratedUuidV7
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "identifying_marks")
class IdentifyingMark(
  @Id
  @GeneratedUuidV7
  @Column(name = "identifying_mark_id", updatable = false, nullable = false)
  val identifyingMarkId: UUID? = null,

  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "body_part_code", referencedColumnName = "id", nullable = false)
  val bodyPart: ReferenceDataCode,

  @ManyToOne
  @JoinColumn(name = "mark_type", referencedColumnName = "id", nullable = false)
  val markType: ReferenceDataCode,

  @ManyToOne
  @JoinColumn(name = "side_code", referencedColumnName = "id")
  val side: ReferenceDataCode,

  @ManyToOne
  @JoinColumn(name = "part_orientation", referencedColumnName = "id")
  val partOrientation: ReferenceDataCode,

  @Column(name = "comment_text")
  val comment: String? = null,

  @OneToMany(mappedBy = "identifyingMark", fetch = EAGER, cascade = [PERSIST, MERGE])
  var photographUuids: Set<IdentifyingMarkImage> = emptySet(),

  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
) {
  fun toDto(): IdentifyingMarkDto = IdentifyingMarkDto(
    id = identifyingMarkId.toString(),
    prisonerNumber,
    bodyPart.toSimpleDto(),
    markType.toSimpleDto(),
    side.toSimpleDto(),
    partOrientation.toSimpleDto(),
    comment,
    photographUuids.map { it.identifyingMarkImageId.toString() },
    createdAt,
    createdBy,
  )
}

@Entity
@Table(name = "identifying_marks_images")
class IdentifyingMarkImage(
  @Id
  @Column(name = "identifying_mark_image_id", updatable = false, nullable = false)
  val identifyingMarkImageId: UUID,

  @ManyToOne
  @JoinColumn(name = "identifying_mark_id", referencedColumnName = "identifying_mark_id")
  val identifyingMark: IdentifyingMark,
)
