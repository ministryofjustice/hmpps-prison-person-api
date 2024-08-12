package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Prison Person")
@JsonInclude(NON_NULL)
data class PrisonPersonDto(
  @Schema(description = "Prisoner Number", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "Physical Attributes (height and weight)")
  val physicalAttributes: PhysicalAttributesDto = PhysicalAttributesDto(),

  @Schema(description = "Health information (Smoker/Vaper, Diet)")
  val health: HealthDto = HealthDto(),
)
