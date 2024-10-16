package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.ZonedDateTime

@Schema(description = "Request object for migration of a prisoner's profile details physical attributes")
data class ProfileDetailsPhysicalAttributesMigrationRequest(
  @Schema(
    description = "Hair type or colour",
  )
  val hair: MigrationValueWithMetadata<String>? = null,

  @Schema(
    description = "Facial hair type",
  )
  val facialHair: MigrationValueWithMetadata<String>? = null,

  @Schema(
    description = "Face shape",
  )
  val face: MigrationValueWithMetadata<String>? = null,

  @Schema(
    description = "Build",
  )
  val build: MigrationValueWithMetadata<String>? = null,

  @Schema(
    description = "Left eye colour",
  )
  val leftEyeColour: MigrationValueWithMetadata<String>? = null,

  @Schema(
    description = "Right eye colour",
  )
  val rightEyeColour: MigrationValueWithMetadata<String>? = null,

  @Schema(
    description = "Shoe size",
  )
  val shoeSize: MigrationValueWithMetadata<String>? = null,

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
  val appliesTo: ZonedDateTime? = null,

  @Schema(
    description = "A flag to indicate this is the latest booking.",
    example = "true",
  )
  val latestBooking: Boolean,
) : Comparable<ProfileDetailsPhysicalAttributesMigrationRequest> {
  override fun compareTo(other: ProfileDetailsPhysicalAttributesMigrationRequest): Int =
    compareValuesBy(
      this,
      other,
      { it.latestBooking },
      { it.appliesTo?.toInstant() ?: if (!it.latestBooking) it.appliesFrom.toInstant() else Instant.MAX },
      { it.appliesFrom },
      { it.hashCode() },
    )
}
