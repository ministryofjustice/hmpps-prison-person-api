package uk.gov.justice.digital.hmpps.prisonperson.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Range

@Schema(description = "Request object for updating a prisoner's physical attributes")
@JsonInclude(NON_NULL)
data class PhysicalAttributesUpdateRequest(
  @Schema(description = "Height (in centimetres). May be left null if no data available for height.", example = "180")
  @field:Range(min = 54, max = 272, message = "The height must be a plausible value in centimetres (between 54 and 272)")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms). May be left null if no data available for weight.", example = "70")
  @field:Range(min = 2, max = 635, message = "The weight must be a plausible value in kilograms (between 2 and 635)")
  val weight: Int? = null,
)
