package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.utils.NullishUtils

@Schema(
  description = "Request object for updating a prisoner's health information. Can include one or multiple fields. " +
          "If an attribute is not defined in the map, it will be set as Undefined.",
)
@JsonInclude(NON_NULL)
data class HealthUpdateRequest(
  @Schema(description = "Map of attributes")
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) : MutableMap<String, Any?> by attributes {
  @Schema(description = "Smoker or vaper. `ReferenceDataCode`.`id`.", example = "SMOKE_NO")
  val smokerOrVaper: Nullish<String> = NullishUtils.getAttribute<String>(attributes, "smokerOrVaper")
}
