package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Distinguishing Mark Sync Response")
class DistinguishingMarkSyncResponse {
  @Schema(description = "The UUID of the distinguishing mark updated", example = "uuid-example")
  val uuid: UUID? = null
}