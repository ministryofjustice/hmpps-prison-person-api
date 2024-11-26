package uk.gov.justice.digital.hmpps.prisonperson.dto.request.distinguishingmarks

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Request object for syncing a prisoner's distinguishing mark image")
@JsonInclude(NON_NULL)
class DistinguishingMarkImageUpdateSyncRequest(
  @Schema(
    description = "Whether the default flag is checked in NOMIS",
    required = true,
  )
  val default: Boolean,

  @Schema(
    description = "The timestamp indicating from when this mark was valid for the prisoner. " +
      "For edits to the current booking, this should be equal to the 'createdAt' date. " +
      "For edits to historical bookings, this should be equal to the booking start date.",
    required = true,
  )
  val appliesFrom: ZonedDateTime,

  @Schema(
    description = "The timestamp of when this mark should no longer be considered applicable. " +
      "For edits to the current booking, this should be left null. " +
      "For edits to historical bookings, this should be equal to the booking end date.",
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
    required = true,
  )
  val createdBy: String,

  @Schema(
    description = "An indication of whether the edit was performed to the latest booking. If this is not the latest " +
      "booking, the edit will be inserted into field history but the distinguishing marks table will not be updated.",
    example = "true",
  )
  val latestBooking: Boolean? = true,
)
