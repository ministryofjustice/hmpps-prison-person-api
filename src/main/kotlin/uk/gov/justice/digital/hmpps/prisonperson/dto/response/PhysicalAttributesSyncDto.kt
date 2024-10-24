package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import com.fasterxml.jackson.annotation.JsonCreator
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Physical Attributes")
data class PhysicalAttributesSyncDto @JsonCreator constructor(
  @Schema(description = "Height (in centimetres)")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms)")
  val weight: Int? = null,

  @Schema(description = "Hair type or colour")
  val hair: String? = null,

  @Schema(description = "Facial hair type")
  val facialHair: String? = null,

  @Schema(description = "Face shape")
  val face: String? = null,

  @Schema(description = "Build")
  val build: String? = null,

  @Schema(description = "Left eye colour")
  val leftEyeColour: String? = null,

  @Schema(description = "Right eye colour")
  val rightEyeColour: String? = null,

  @Schema(description = "Shoe size")
  val shoeSize: String? = null,
)
