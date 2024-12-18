package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto

@Schema(description = "Prison person health")
data class HealthDto(
  @Schema(description = "Smoker or vaper")
  val smokerOrVaper: ValueWithMetadata<ReferenceDataSimpleDto?>? = null,

  @Schema(description = "Food allergies")
  val foodAllergies: ValueWithMetadata<List<ReferenceDataSimpleDto>>? = null,

  @Schema(description = "Medical dietary requirements")
  val medicalDietaryRequirements: ValueWithMetadata<List<ReferenceDataSimpleDto>>? = null,
)
