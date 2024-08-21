package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import uk.gov.justice.digital.hmpps.prisonperson.utils.NullishUtils.Companion.getAttribute

@Schema(
  description = "Request object for updating a prisoner's health information. Can include one or multiple fields. " +
    "If an attribute is provided and set to 'null' it will be updated equal to 'null'. " +
    "If it is not provided it is not updated",
)
@JsonInclude(NON_NULL)
data class HealthUpdateRequest(
  @Schema(hidden = true)
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) {
  @Schema(
    description = "Smoker or vaper. `ReferenceDataCode`.`id`.",
    type = "string",
    example = "SMOKE_NO",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val smokerOrVaper: Nullish<String> = getAttribute(attributes, "smokerOrVaper")
}
