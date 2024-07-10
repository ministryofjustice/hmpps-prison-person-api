package uk.gov.justice.digital.hmpps.prisonperson.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Physical Attributes Sync Response")
data class PhysicalAttributesSyncResponse(
  @Schema(description = "The IDs of field history inserted during the sync", example = "123")
  val fieldHistoryInserted: List<Long> = listOf(),
)
