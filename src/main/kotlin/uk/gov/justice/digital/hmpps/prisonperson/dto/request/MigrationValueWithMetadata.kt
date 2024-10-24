package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "The migration value (`ReferenceDataCode.code`) with metadata")
data class MigrationValueWithMetadata<T> @JsonCreator constructor(
  @Schema(description = "Value")
  val value: T?,

  @Schema(description = "The timestamp indicating when this record was last edited in NOMIS")
  val lastModifiedAt: ZonedDateTime,

  @Schema(description = "The username of who last edited the record in NOMIS", example = "USER1")
  val lastModifiedBy: String,
)
