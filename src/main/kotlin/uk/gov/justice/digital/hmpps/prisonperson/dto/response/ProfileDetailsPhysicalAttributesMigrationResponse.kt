package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Profile Details Physical Attributes Migration Response")
data class ProfileDetailsPhysicalAttributesMigrationResponse(
  @Schema(description = "The IDs of field history created during the migration", example = "[123, 456]")
  val fieldHistoryInserted: List<Long> = listOf(),
)
