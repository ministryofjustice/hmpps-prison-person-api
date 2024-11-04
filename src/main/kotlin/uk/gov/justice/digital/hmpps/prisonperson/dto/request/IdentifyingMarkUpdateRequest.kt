package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.utils.Nullish
import uk.gov.justice.digital.hmpps.prisonperson.utils.getAttributeAsNullish

@Schema(
  description = "Request object for updating a prisoner's identifying mark. Can include one or multiple fields. " +
    "If an attribute is provided and set to 'null' it will be updated equal to 'null'. " +
    "If it is not provided it is not updated",
)
@JsonInclude(NON_NULL)
class IdentifyingMarkUpdateRequest(
  @Schema(hidden = true)
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) {
  @Schema(
    description = "Type of identifying mark. `ReferenceDataCode.id`.",
    example = "MARK_TYPE_SCAR",
  )
  val markType: Nullish<String> = getAttributeAsNullish(attributes, "markType")

  @Schema(
    description = "Part of body the identifying mark is on. `ReferenceDataCode.id`.",
    example = "BODY_PART_HEAD",
  )
  val bodyPart: Nullish<String> = getAttributeAsNullish(attributes, "bodyPart")

  @Schema(
    description = "Side of the body part the mark is on. `ReferenceDataCode.id`.",
    example = "SIDE_R",
  )
  val side: Nullish<String> = getAttributeAsNullish(attributes, "side")

  @Schema(
    description = "Orientation of the mark on the body part. `ReferenceDataCode.id`.",
    example = "PART_ORIENT_CENTR",
  )
  val partOrientation: Nullish<String> = getAttributeAsNullish(attributes, "partOrientation")

  @Schema(
    description = "Comment about the identifying mark.",
    example = "Long healed scar from an old fight",
  )
  val comment: Nullish<String> = getAttributeAsNullish(attributes, "comment")
}