package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Physical Attributes Migration Response")
data class PhysicalAttributesMigrationResponse(
  @Schema(description = "The IDs of field history created during the migration", example = "[123, 456]")
  val fieldHistoryInserted: List<Long> = listOf(),
)
