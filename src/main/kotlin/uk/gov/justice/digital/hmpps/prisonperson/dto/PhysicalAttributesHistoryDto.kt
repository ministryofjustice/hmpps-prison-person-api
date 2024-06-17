package uk.gov.justice.digital.hmpps.prisonperson.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Physical Attributes History")
data class PhysicalAttributesHistoryDto(

  @Schema(description = "Unique identifier for the history entry", example = "123")
  val physicalAttributesHistoryId: Long,

  @Schema(description = "Height (in centimetres)", example = "180")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms)", example = "70")
  val weight: Int? = null,

  @Schema(description = "The timestamp indicating from when these physical attributes were true for the prisoner")
  val appliesFrom: ZonedDateTime,

  @Schema(
    description = "The timestamp of when these physical attributes were superseded by another record. " +
      "If they are currently applicable, this is null.",
  )
  var appliesTo: ZonedDateTime? = null,

  @Schema(description = "The timestamp indicating when this record was created")
  val createdAt: ZonedDateTime,

  @Schema(description = "The username of who created the record")
  val createdBy: String,
)
