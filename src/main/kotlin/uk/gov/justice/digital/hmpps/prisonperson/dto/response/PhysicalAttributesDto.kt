package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto

@Schema(description = "Physical Attributes")
data class PhysicalAttributesDto(
  @Schema(description = "Height (in centimetres)", example = "180")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms)", example = "70")
  val weight: Int? = null,

  @Schema(description = "Hair type or colour")
  val hair: ReferenceDataCodeDto? = null,

  @Schema(description = "Facial hair type")
  val facialHair: ReferenceDataCodeDto? = null,

  @Schema(description = "Face shape")
  val face: ReferenceDataCodeDto? = null,

  @Schema(description = "Build")
  val build: ReferenceDataCodeDto? = null,

  @Schema(description = "Left eye colour")
  val leftEyeColour: ReferenceDataCodeDto? = null,

  @Schema(description = "Right eye colour")
  val rightEyeColour: ReferenceDataCodeDto? = null,
)
