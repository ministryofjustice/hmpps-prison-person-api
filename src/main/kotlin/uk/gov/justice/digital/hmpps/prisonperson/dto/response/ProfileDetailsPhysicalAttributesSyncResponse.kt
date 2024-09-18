package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Profile Details Physical Attributes Sync Response")
data class ProfileDetailsPhysicalAttributesSyncResponse(
  @Schema(description = "The IDs of field history inserted during the sync", example = "[123, 456]")
  val fieldHistoryInserted: List<Long> = listOf(),
)
