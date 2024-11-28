package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import org.openapitools.jackson.nullable.JsonNullable
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableRange
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableShoeSize

@Schema(
  description = "Request object for updating a prisoner's physical attributes. Can include one or multiple attributes. " +
    "If an attribute is not provided, it will not be updated.  If an attribute is provided and set to 'null' it will be" +
    " updated to equal 'null'.",
)
@JsonInclude(NON_NULL)
data class PhysicalAttributesUpdateRequest(
  @Schema(
    description = "Height (in centimetres).",
    example = "180",
    type = "integer",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:JsonNullableRange(
    min = 50,
    max = 280,
    message = "The height must be a plausible value in centimetres (between 50 and 280), null or not provided",
  )
  val height: JsonNullable<Int?> = JsonNullable.undefined(),

  @Schema(
    description = "Weight (in kilograms).",
    example = "70",
    type = "integer",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:JsonNullableRange(
    min = 12,
    max = 640,
    message = "The weight must be a plausible value in kilograms (between 12 and 640), null or not provided",
  )
  val weight: JsonNullable<Int?> = JsonNullable.undefined(),

  @Schema(
    description = "Hair type or colour. `ReferenceDataCode.id`.",
    example = "HAIR_BROWN",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val hair: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Facial hair type. `ReferenceDataCode.id`.",
    example = "FACIAL_HAIR_BEARDED",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val facialHair: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Face shape. `ReferenceDataCode.id`.",
    example = "FACE_OVAL",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val face: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Build. `ReferenceDataCode.id`.",
    example = "BUILD_MEDIUM",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val build: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Left eye colour. `ReferenceDataCode.id`.",
    example = "EYE_GREEN",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val leftEyeColour: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Right eye colour. `ReferenceDataCode.id`.",
    example = "EYE_BLUE",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val rightEyeColour: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Shoe size (in UK half sizes).",
    example = "9.5",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:JsonNullableShoeSize(
    min = "1",
    max = "25",
    message = "The shoe size must be a whole or half size between 1 and 25, null or not provided",
  )
  val shoeSize: JsonNullable<String?> = JsonNullable.undefined(),
)
