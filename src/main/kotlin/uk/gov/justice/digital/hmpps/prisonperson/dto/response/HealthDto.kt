package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import java.time.ZonedDateTime

@Schema(description = "Prison person health")
data class HealthDto(
  @Schema(description = "Smoker or vaper")
  val smokerOrVaper: ValueWithMetadata<ReferenceDataCodeDto?>? = null,
)
