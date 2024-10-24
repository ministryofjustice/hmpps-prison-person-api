package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(
  description = "Request object for retrieving a prisoner's photograph data.",
)
data class PhotographsForPrisonerRequest @JsonCreator constructor(
  @Schema(description = "The prisoner number.", example = "A1234AA")
  @field:NotNull
  val prisonerNumber: String,

  @Schema(description = "The active case load of the user making the request.", example = "MDI")
  @field:NotNull
  val activeCaseLoadId: String,

  @Schema(description = "The username of the user making the request.", example = "USER1")
  @field:NotNull
  val username: String,
)
