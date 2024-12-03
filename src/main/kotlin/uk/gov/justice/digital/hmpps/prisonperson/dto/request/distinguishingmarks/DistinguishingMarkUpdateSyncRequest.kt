package uk.gov.justice.digital.hmpps.prisonperson.dto.request.distinguishingmarks

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Request object for syncing a prisoner's distinguishing marks")
@JsonInclude(NON_NULL)
class DistinguishingMarkUpdateSyncRequest(
  @Schema(
    description = "The UUID of the distinguishing mark to update.",
    example = "af168736-d276-46a1-8038-e2cb84da4cdf",
    required = true,
  )
  uuid: UUID,

  @Schema(
    description = "Type of distinguishing mark. `ReferenceDataCode.id`.",
    example = "MARK_TYPE_SCAR",
  )
  @NotNull("Mark type is required.")
  val markType: String,

  @Schema(
    description = "Part of body the distinguishing mark is on. `ReferenceDataCode.id`.",
    example = "BODY_PART_HEAD",
  )
  @NotNull("Body part is required.")
  val bodyPart: String,

  @Schema(
    description = "Whether or not the image for the distinguishing mark has been updated, to inform us if we should update our stored image.",
    example = "true",
  )
  val imageUpdated: Boolean?,

  @Schema(
    description = "Side of the body part the mark is on. `ReferenceDataCode.id`. May be left null if unentered.",
    example = "SIDE_R",
  )
  val side: String?,

  @Schema(
    description = "Orientation of the mark on the body part. `ReferenceDataCode.id`. May be left null if unentered.",
    example = "PART_ORIENT_CENTR",
  )
  val partOrientation: String?,

  @Schema(
    description = "Comment about the distinguishing mark. May be left null if unentered.",
    example = "Long healed scar from an old fight",
  )
  val comment: String?,

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