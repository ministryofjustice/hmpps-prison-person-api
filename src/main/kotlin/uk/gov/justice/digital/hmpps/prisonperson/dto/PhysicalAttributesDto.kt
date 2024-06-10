package uk.gov.justice.digital.hmpps.prisonperson.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Physical Attributes")
@JsonInclude(NON_NULL)
data class PhysicalAttributesDto(
  @Schema(description = "Height (in centimetres)", example = "180")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms)", example = "70")
  val weight: Int? = null,
)
