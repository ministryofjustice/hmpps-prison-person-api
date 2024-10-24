package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishRange
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishShoeSize
import uk.gov.justice.digital.hmpps.prisonperson.utils.Nullish
import uk.gov.justice.digital.hmpps.prisonperson.utils.getAttributeAsNullish

@Schema(
  description = "Request object for updating a prisoner's physical attributes. Can include one or multiple attributes. " +
    "If an attribute is not provided, it will not be updated.  If an attribute is provided and set to 'null' it will be" +
    " updated to equal 'null'.",
)
@JsonInclude(NON_NULL)
data class PhysicalAttributesUpdateRequest @JsonCreator constructor(
  @Schema(hidden = true)
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) {
  @Schema(
    description = "Height (in centimetres).",
    example = "180",
    type = "integer",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:NullishRange(
    min = 50,
    max = 280,
    message = "The height must be a plausible value in centimetres (between 50 and 280), null or not provided",
  )
  val height: Nullish<Int> = getAttributeAsNullish<Int>(attributes, "height")

  @Schema(
    description = "Weight (in kilograms).",
    example = "70",
    type = "integer",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:NullishRange(
    min = 12,
    max = 640,
    message = "The weight must be a plausible value in kilograms (between 12 and 640), null or not provided",
  )
  val weight: Nullish<Int> = getAttributeAsNullish<Int>(attributes, "weight")

  @Schema(
    description = "Hair type or colour. `ReferenceDataCode.id`.",
    example = "HAIR_BROWN",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val hair: Nullish<String> = getAttributeAsNullish<String>(attributes, "hair")

  @Schema(
    description = "Facial hair type. `ReferenceDataCode.id`.",
    example = "FACIAL_HAIR_BEARDED",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val facialHair: Nullish<String> = getAttributeAsNullish<String>(attributes, "facialHair")

  @Schema(
    description = "Face shape. `ReferenceDataCode.id`.",
    example = "FACE_OVAL",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val face: Nullish<String> = getAttributeAsNullish<String>(attributes, "face")

  @Schema(
    description = "Build. `ReferenceDataCode.id`.",
    example = "BUILD_MEDIUM",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val build: Nullish<String> = getAttributeAsNullish<String>(attributes, "build")

  @Schema(
    description = "Left eye colour. `ReferenceDataCode.id`.",
    example = "EYE_GREEN",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val leftEyeColour: Nullish<String> = getAttributeAsNullish<String>(attributes, "leftEyeColour")

  @Schema(
    description = "Right eye colour. `ReferenceDataCode.id`.",
    example = "EYE_BLUE",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val rightEyeColour: Nullish<String> = getAttributeAsNullish<String>(attributes, "rightEyeColour")

  @Schema(
    description = "Shoe size (in UK half sizes).",
    example = "9.5",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:NullishShoeSize(
    min = "1",
    max = "25",
    message = "The shoe size must be a whole or half size between 1 and 25, null or not provided",
  )
  val shoeSize: Nullish<String> = getAttributeAsNullish<String>(attributes, "shoeSize")
}
