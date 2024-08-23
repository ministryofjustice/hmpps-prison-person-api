package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto

@Schema(description = "Prison person health")
data class PrisonerHealthDto(
  @Schema(description = "Smoker or vaper")
  val smokerOrVaper: ValueWithMetadata<ReferenceDataSimpleDto?>? = null,
)
