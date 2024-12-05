package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import org.openapitools.jackson.nullable.JsonNullable
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableReferenceDataCode

@Schema(
  description = "Request object for updating a prisoner's distinguishing mark. Can include one or multiple fields. " +
    "If an attribute is provided and set to 'null' it will be updated equal to 'null'. " +
    "If it is not provided it is not updated",
)
@JsonInclude(NON_NULL)
data class DistinguishingMarkUpdateRequest(
  @Schema(
    description = "Type of distinguishing mark. `ReferenceDataCode.id`.",
    example = "MARK_TYPE_SCAR",
    nullable = false,
    requiredMode = Schema.RequiredMode.NOT_REQUIRED,
  )
  @field:JsonNullableReferenceDataCode(
    domains = ["MARK_TYPE"],
    allowNull = false,
    message = "Type of distinguishing mark should a reference data code ID in the correct domain, or Undefined.",
  )
  val markType: JsonNullable<String> = JsonNullable.undefined(),

  @Schema(
    description = "Part of body the distinguishing mark is on. `ReferenceDataCode.id`.",
    example = "BODY_PART_HEAD",
    nullable = false,
    requiredMode = Schema.RequiredMode.NOT_REQUIRED,
  )
  @field:JsonNullableReferenceDataCode(
    domains = ["BODY_PART"],
    allowNull = false,
    message = "Body part of distinguishing mark should a reference data code ID in the correct domain, or Undefined.",
  )
  val bodyPart: JsonNullable<String> = JsonNullable.undefined(),

  @Schema(
    description = "Side of the body part the mark is on. `ReferenceDataCode.id`.",
    example = "SIDE_R",
    nullable = true,
    requiredMode = Schema.RequiredMode.NOT_REQUIRED,
  )
  @field:JsonNullableReferenceDataCode(
    domains = ["SIDE"],
  )
  val side: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Orientation of the mark on the body part. `ReferenceDataCode.id`.",
    example = "PART_ORIENT_CENTR",
    nullable = true,
    requiredMode = Schema.RequiredMode.NOT_REQUIRED,
  )
  @field:JsonNullableReferenceDataCode(
    domains = ["PART_ORIENT"],
  )
  val partOrientation: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "Comment about the distinguishing mark.",
    example = "Long healed scar from an old fight",
    nullable = true,
    requiredMode = Schema.RequiredMode.NOT_REQUIRED,
  )
  val comment: JsonNullable<String?> = JsonNullable.undefined(),
)
