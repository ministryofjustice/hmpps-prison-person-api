package uk.gov.justice.digital.hmpps.prisonperson.dto.history

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import java.time.ZonedDateTime

@Schema(description = "Distinguishing mark - History item")
data class DistinguishingMarkHistoryDto(
  @Schema(
    description = "The prisoner number of the prisoner this distinguishing mark is associated with",
    example = "A1234AA",
  )
  val prisonerNumber: String,

  @Schema(description = "The body part the mark is on")
  val bodyPart: ReferenceDataSimpleDto,

  @Schema(description = "The type of distinguishing mark (e.g. tattoo, scar)")
  val markType: ReferenceDataSimpleDto,

  @Schema(description = "The side of the body part the mark is on")
  val side: ReferenceDataSimpleDto? = null,

  @Schema(description = "The orientation of the mark on the body part (e.g. Centre, Low, Upper)")
  val partOrientation: ReferenceDataSimpleDto? = null,

  @Schema(description = "Comment about the distinguishing mark")
  val comment: String? = null,

  @Schema(description = "List of photograph UUIDs associated with this distinguishing mark")
  val photographUuids: List<DistinguishingMarkImageHistoryDto> = emptyList(),

  @Schema(
    description = "The date and time the data was last modified",
  )
  @JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ",
  )
  val createdAt: ZonedDateTime,

  @Schema(description = "Username of the user that last modified this field", example = "USER1")
  val createdBy: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DistinguishingMarkHistoryDto

    if (prisonerNumber != other.prisonerNumber) return false
    if (bodyPart != other.bodyPart) return false
    if (markType != other.markType) return false
    if (side != other.side) return false
    if (partOrientation != other.partOrientation) return false
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
}

@Schema(description = "Distinguishing mark image - History item")
data class DistinguishingMarkImageHistoryDto(
  @Schema(description = "The uuid of the distinguishing mark")
  val id: String,

  @Schema(description = "Whether the distinguishing mark is the latest one or not")
  val latest: Boolean,
)
