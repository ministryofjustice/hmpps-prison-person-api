package uk.gov.justice.digital.hmpps.prisonperson.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Prison Person")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PrisonPersonDto(
  @Schema(description = "Prisoner Number", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "Physical Attributes (height and weight)")
  val physicalAttributes: PhysicalAttributesDto = PhysicalAttributesDto(),
)
