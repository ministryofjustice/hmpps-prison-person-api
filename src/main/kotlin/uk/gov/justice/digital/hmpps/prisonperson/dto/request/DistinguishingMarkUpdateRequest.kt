package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.utils.Nullish
import uk.gov.justice.digital.hmpps.prisonperson.utils.getAttributeAsNullish

@Schema(
  description = "Request object for updating a prisoner's distinguishing mark. Can include one or multiple fields. " +
    "If an attribute is provided and set to 'null' it will be updated equal to 'null'. " +
    "If it is not provided it is not updated",
)
@JsonInclude(NON_NULL)
class DistinguishingMarkUpdateRequest(
  @Schema(hidden = true)
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) {
  @Schema(
    description = "Type of distinguishing mark. `ReferenceDataCode.id`.",
    example = "MARK_TYPE_SCAR",
  )
  @field:NullishReferenceDataCode(
    domains = ["MARK_TYPE"],
    allowNull = false,
    message = "Type of distinguishing mark should a reference data code ID in the correct domain, or Undefined.",
  )
  val markType: Nullish<String> = getAttributeAsNullish(attributes, "markType")

  @Schema(
    description = "Part of body the distinguishing mark is on. `ReferenceDataCode.id`.",
    example = "BODY_PART_HEAD",
  )
  @field:NullishReferenceDataCode(
    domains = ["BODY_PART"],
    allowNull = false,
    message = "Body part of distinguishing mark should a reference data code ID in the correct domain, or Undefined.",
  )
  val bodyPart: Nullish<String> = getAttributeAsNullish(attributes, "bodyPart")

  @Schema(
    description = "Side of the body part the mark is on. `ReferenceDataCode.id`.",
    example = "SIDE_R",
  )
  @field:NullishReferenceDataCode(
    domains = ["SIDE"],
  )
  val side: Nullish<String> = getAttributeAsNullish(attributes, "side")

  @Schema(
    description = "Orientation of the mark on the body part. `ReferenceDataCode.id`.",
    example = "PART_ORIENT_CENTR",
  )
  @field:NullishReferenceDataCode(
    domains = ["PART_ORIENT"],
  )
  val partOrientation: Nullish<String> = getAttributeAsNullish(attributes, "partOrientation")

  @Schema(
    description = "Comment about the distinguishing mark.",
    example = "Long healed scar from an old fight",
  )
  val comment: Nullish<String> = getAttributeAsNullish(attributes, "comment")
}
