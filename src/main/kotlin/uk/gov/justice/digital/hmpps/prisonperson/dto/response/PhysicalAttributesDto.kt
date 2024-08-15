package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import java.time.ZonedDateTime

@Schema(description = "Physical Attributes")
data class PhysicalAttributesDto(
  @Schema(description = "Height (in centimetres)")
  val height: ValueWithMetadata<Int?>? = null,

  @Schema(description = "Weight (in kilograms)")
  val weight: ValueWithMetadata<Int?>? = null,

  @Schema(description = "Hair type or colour")
  val hair: ValueWithMetadata<ReferenceDataCodeDto?>? = null,

  @Schema(description = "Facial hair type")
  val facialHair: ValueWithMetadata<ReferenceDataCodeDto?>? = null,

  @Schema(description = "Face shape")
  val face: ValueWithMetadata<ReferenceDataCodeDto?>? = null,

  @Schema(description = "Build")
  val build: ValueWithMetadata<ReferenceDataCodeDto?>? = null,

  @Schema(description = "Left eye colour")
  val leftEyeColour: ValueWithMetadata<ReferenceDataCodeDto?>? = null,

  @Schema(description = "Right eye colour")
  val rightEyeColour: ValueWithMetadata<ReferenceDataCodeDto?>? = null,

  @Schema(description = "Shoe size")
  val shoeSize: ValueWithMetadata<String?>? = null,
)

data class ValueWithMetadata<T>(
  @Schema(description = "Value")
  val value: T?,

  @Schema(description = "Timestamp this field was last modified")
  val lastModifiedAt: ZonedDateTime,

  @Schema(description = "Username of the user that last modified this field", example = "USER1")
  val lastModifiedBy: String,
)
