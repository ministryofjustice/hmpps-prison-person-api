package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull

@Schema(
  description = "Request object for creating an identifying mark for a prisoner.",
)
@JsonInclude(NON_NULL)
class IdentifyingMarkRequest(
  @Schema(
    description = "The prisoner number of the prisoner this identifying mark is associated with.",
    example = "A1234AA",
  )
  @NotNull("Prisoner number is required.")
  val prisonerNumber: String,

  @Schema(
    description = "Type of identifying mark. `ReferenceDataCode.id`.",
    example = "MARK_TYPE_SCAR",
  )
  @NotNull("Mark type is required.")
  val markType: String,

  @Schema(
    description = "Part of body the identifying mark is on. `ReferenceDataCode.id`.",
    example = "BODY_PART_HEAD",
  )
  @NotNull("Body part is required.")
  val bodyPart: String,

  @Schema(
    description = "Side of the body part the mark is on. `ReferenceDataCode.id`.",
    example = "SIDE_R",
  )
  val side: String?,

  @Schema(
    description = "Orientation of the mark on the body part. `ReferenceDataCode.id`.",
    example = "PART_ORIENT_CENTR",
  )
  val partOrientation: String?,

  @Schema(
    description = "Comment about the identifying mark.",
    example = "Long healed scar from an old fight",
  )
  val comment: String?,
)
