package uk.gov.justice.digital.hmpps.prisonperson.jpa

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.CascadeType.MERGE
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.EAGER
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.hibernate.annotations.SortNatural
import uk.gov.justice.digital.hmpps.prisonperson.dto.history.DistinguishingMarkHistoryDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.history.DistinguishingMarkImageHistoryDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkImageDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.DistinguishingMarkImageRepository
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.utils.GeneratedUuidV7
import java.time.ZonedDateTime
import java.util.SortedSet
import java.util.UUID

@Entity
class DistinguishingMark(
  @Id
  @GeneratedUuidV7
  @Column(name = "distinguishing_mark_id", updatable = false, nullable = false)
  val distinguishingMarkId: UUID? = null,

  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne(fetch = LAZY)
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
  var photographUuids: MutableList<DistinguishingMarkImage> = mutableListOf(),

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,

  // History

  // Stores snapshots of each update to a distinguishing mark
  @OneToMany(mappedBy = "mark", fetch = LAZY, orphanRemoval = true, cascade = [ALL])
  @SortNatural
  val history: SortedSet<DistinguishingMarkHistory> = sortedSetOf(),
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

  fun toHistoryDto(): DistinguishingMarkHistoryDto = DistinguishingMarkHistoryDto(
    prisonerNumber,
    bodyPart.toSimpleDto(),
    markType.toSimpleDto(),
    side?.toSimpleDto(),
    partOrientation?.toSimpleDto(),
    comment,
    photographUuids.map { it.toHistoryDto() },
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

  fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
  ) =
    updateFieldHistory(lastModifiedAt, null, lastModifiedAt, lastModifiedBy, DPS)

  fun updateFieldHistory(
    appliesFrom: ZonedDateTime,
    appliesTo: ZonedDateTime?,
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
    source: Source = DPS,
    migratedAt: ZonedDateTime? = null,
    anomalous: Boolean? = null,
  ) {
    val previousVersion = history.lastOrNull()
    if (previousVersion == null || previousVersion.valueJson != this.toHistoryDto()) {
      previousVersion?.updateAppliesTo(appliesFrom, lastModifiedAt)

      history.add(
        DistinguishingMarkHistory(
          mark = this,
          valueJson = this.toHistoryDto(),
          appliesFrom = appliesFrom,
          appliesTo = appliesTo,
          createdAt = lastModifiedAt,
          createdBy = lastModifiedBy,
          source = source,
          migratedAt = migratedAt,
          anomalous = anomalous == true,
        ),
      )
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DistinguishingMark

    if (prisonerNumber != other.prisonerNumber) return false
    if (bodyPart.id != other.bodyPart.id) return false
    if (markType.id != other.markType.id) return false
    if (side?.id != other.side?.id) return false
    if (partOrientation?.id != other.partOrientation?.id) return false
    if (comment != other.comment) return false
    if (photographUuids.toList() != other.photographUuids.toList()) return false
    if (createdAt.toInstant() != other.createdAt.toInstant()) return false
    if (createdBy != other.createdBy) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + bodyPart.hashCode()
    result = 31 * result + markType.hashCode()
    result = 31 * result + (side?.hashCode() ?: 0)
    result = 31 * result + (partOrientation?.hashCode() ?: 0)
    result = 31 * result + (comment?.hashCode() ?: 0)
    result = 31 * result + photographUuids.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + createdBy.hashCode()
    return result
  }

  override fun toString(): String {
    return "DistinguishingMark(distinguishingMarkId=$distinguishingMarkId, prisonerNumber='$prisonerNumber', bodyPart=$bodyPart, markType=$markType, side=$side, partOrientation=$partOrientation, comment=$comment, photographUuids=$photographUuids, createdAt=$createdAt, createdBy='$createdBy')"
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

  fun toHistoryDto(): DistinguishingMarkImageHistoryDto = DistinguishingMarkImageHistoryDto(
    id = distinguishingMarkImageId.toString(),
    latest = latest,
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DistinguishingMarkImage

    if (distinguishingMarkImageId != other.distinguishingMarkImageId) return false
    if (latest != other.latest) return false
    if (distinguishingMark.distinguishingMarkId != other.distinguishingMark.distinguishingMarkId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = latest.hashCode()
    result = 31 * result + distinguishingMark.distinguishingMarkId.hashCode()
    return result
  }

  override fun toString(): String {
    return "DistinguishingMarkImage(distinguishingMarkImageId=$distinguishingMarkImageId, latest=$latest)"
  }
}
