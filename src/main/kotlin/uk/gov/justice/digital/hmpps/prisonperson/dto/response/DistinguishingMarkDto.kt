package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import java.time.ZonedDateTime

@Schema(description = "Distinguishing mark")
data class DistinguishingMarkDto(
  @Schema(description = "The id of the distinguishing mark")
  val id: String,

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
  val photographUuids: List<DistinguishingMarkImageDto> = emptyList(),

  @Schema(
    description = "The date and time the data was last modified",
  )
  val createdAt: ZonedDateTime,

  @Schema(description = "Username of the user that last modified this field", example = "USER1")
  val createdBy: String,
)

@Schema(description = "Distinguishing mark image")
data class DistinguishingMarkImageDto(
  @Schema(description = "The uuid of the distinguishing mark")
  val id: String,

  @Schema(description = "Whether the distinguishing mark is the latest one or not")
  val latest: Boolean,
)
