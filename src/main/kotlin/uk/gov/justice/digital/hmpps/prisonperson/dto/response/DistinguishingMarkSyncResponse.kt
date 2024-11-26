package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Distinguishing Mark Sync Response")
class DistinguishingMarkSyncResponse(
  @Schema(description = "The UUID of the distinguishing mark updated", example = "22198ef9-445d-449a-b016-0521ebfb5c2d")
  val uuid: UUID,
)