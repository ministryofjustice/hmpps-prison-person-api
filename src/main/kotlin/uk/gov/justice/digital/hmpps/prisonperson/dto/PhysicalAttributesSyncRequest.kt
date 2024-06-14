package uk.gov.justice.digital.hmpps.prisonperson.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Request object for syncing a prisoner's physical attributes")
@JsonInclude(NON_NULL)
data class PhysicalAttributesSyncRequest(
  @Schema(
    description = "Height (in centimetres). May be left null if no data available for height.",
    example = "180",
    nullable = true,
  )
  private val height: Int? = null,

  @Schema(
    description = "Weight (in kilograms). May be left null if no data available for weight.",
    example = "70",
    nullable = true,
  )
  val weight: Int? = null,

  @Schema(
    description = "The timestamp indicating from when these physical attributes were true for the prisoner",
    required = true,
  )
  val appliesFrom: ZonedDateTime,

  @Schema(
    description = "The timestamp of when these physical attributes were superseded by another record. " +
      "If they are current, this should be left null.",
    nullable = true,
  )
  var appliesTo: ZonedDateTime? = null,

  @Schema(
    description = "The timestamp indicating when this record was created in NOMIS.",
    required = true,
  )
  val createdAt: ZonedDateTime,

  @Schema(
    description = "The username of who created the record in NOMIS, if available.",
    example = "USER1",
    nullable = true,
  )
  val createdBy: String? = null,
)
