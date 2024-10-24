package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import com.fasterxml.jackson.annotation.JsonCreator
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto

@Schema(description = "Prison person health")
data class HealthDto @JsonCreator constructor(
  @Schema(description = "Smoker or vaper")
  val smokerOrVaper: ValueWithMetadata<ReferenceDataSimpleDto?>? = null,

  @Schema(description = "Food allergies")
  val foodAllergies: List<ReferenceDataSimpleDto?> = emptyList(),

  @Schema(description = "Medical dietary requirements")
  val medicalDietaryRequirements: List<ReferenceDataSimpleDto?> = emptyList(),
)
