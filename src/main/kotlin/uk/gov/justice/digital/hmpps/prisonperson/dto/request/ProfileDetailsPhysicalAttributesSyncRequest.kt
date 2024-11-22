package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Request object for syncing a prisoner's profile details physical attributes")
@JsonInclude(NON_NULL)
data class ProfileDetailsPhysicalAttributesSyncRequest(
  @Schema(
    description = "Hair type or colour",
  )
  val hair: SyncValueWithMetadata<String>? = null,

  @Schema(
    description = "Facial hair type",
  )
  val facialHair: SyncValueWithMetadata<String>? = null,

  @Schema(
    description = "Face shape",
  )
  val face: SyncValueWithMetadata<String>? = null,

  @Schema(
    description = "Build",
  )
  val build: SyncValueWithMetadata<String>? = null,

  @Schema(
    description = "Left eye colour",
  )
  val leftEyeColour: SyncValueWithMetadata<String>? = null,

  @Schema(
    description = "Right eye colour",
  )
  val rightEyeColour: SyncValueWithMetadata<String>? = null,

  @Schema(
    description = "Shoe size",
  )
  val shoeSize: SyncValueWithMetadata<String>? = null,

  @Schema(
    description = "The timestamp indicating from when these profile details physical attributes were true for the prisoner. " +
      "For edits to the current booking, this should be equal to the 'createdAt' date. " +
      "For edits to historical bookings, this should be equal to the booking start date.",
    required = true,
  )
  val appliesFrom: ZonedDateTime,

  @Schema(
    description = "The timestamp of when these profile details physical attributes should no longer be considered applicable. " +
      "For edits to the current booking, this should be left null. " +
      "For edits to historical bookings, this should be equal to the booking end date.",
    nullable = true,
  )
  var appliesTo: ZonedDateTime? = null,

  @Schema(
    description = "An indication of whether the edit was performed to the latest booking. If this is not the latest " +
      "booking, the edit will be inserted into field history but the physical attributes table will not be updated.",
    example = "true",
  )
  val latestBooking: Boolean? = true,

  @Schema(
    description = "An indication of whether the sync is to correct the current physical attributes. " +
      "This is used, for example, when a 'move booking' event occurs where we are correcting an update that was made to " +
      "the wrong prisoner record. The values will be corrected and the history item will be removed",
    example = "false",
  )
  val correction: Boolean? = false,
)
