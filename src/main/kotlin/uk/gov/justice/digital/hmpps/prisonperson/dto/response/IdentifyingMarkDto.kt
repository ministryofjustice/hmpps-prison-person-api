package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import com.fasterxml.jackson.annotation.JsonCreator
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import java.time.ZonedDateTime

@Schema(description = "Identifying Mark")
data class IdentifyingMarkDto @JsonCreator constructor(
  @Schema(description = "The id of the identifying mark")
  val id: String,

  @Schema(description = "The prisoner number of the prisoner this identifying mark is associated with", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "The body part the mark is on")
  val bodyPart: ReferenceDataSimpleDto,

  @Schema(description = "The type of identifying mark (e.g. tattoo, scar)")
  val markType: ReferenceDataSimpleDto,

  @Schema(description = "The side of the body part the mark is on")
  val side: ReferenceDataSimpleDto? = null,

  @Schema(description = "The orientation of the mark on the body part (e.g. Centre, Low, Upper)")
  val partOrientation: ReferenceDataSimpleDto? = null,

  @Schema(description = "Comment about the identifying mark")
  val comment: String? = null,

  @Schema(description = "List of photograph UUIDs associated with this identifying mark")
  val photographUuids: List<String> = emptyList(),

  @Schema(
    description = "The date and time the data was last modified",
  )
  val createdAt: ZonedDateTime,

  @Schema(description = "Username of the user that last modified this field", example = "USER1")
  val createdBy: String,
)
