package uk.gov.justice.digital.hmpps.prisonperson.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Request object for migration of a prisoner's physical attributes")
@JsonInclude(NON_NULL)
data class PhysicalAttributesMigrationRequest(
  @Schema(
    description = "Height (in centimetres). May be left null if no data available for height.",
    example = "180",
    nullable = true,
  )
  val height: Int? = null,

  @Schema(
    description = "Weight (in kilograms). May be left null if no data available for weight.",
    example = "70",
    nullable = true,
  )
  val weight: Int? = null,

  @Schema(
    description = "The timestamp indicating from when these physical attributes were true for the prisoner. " +
      "For migration of the current booking, this should be equal to the 'createdAt' date. " +
      "For migration of historical bookings, this should be equal to the booking start date.",
    required = true,
  )
  val appliesFrom: ZonedDateTime,

  @Schema(
    description = "The timestamp of when these physical attributes should no longer be considered applicable. " +
      "For migration of the current booking, this should be left null. " +
      "For migration of historical bookings, this should be equal to the booking end date.",
    nullable = true,
  )
  var appliesTo: ZonedDateTime? = null,

  @Schema(
    description = "The timestamp indicating when this record was last edited in NOMIS.",
    required = true,
  )
  val createdAt: ZonedDateTime,

  @Schema(
    description = "The username of who last edited the record in NOMIS",
    example = "USER1",
    nullable = true,
  )
  val createdBy: String,
)
