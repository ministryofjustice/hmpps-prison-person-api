package uk.gov.justice.digital.hmpps.prisonperson.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Reference Data Code")
data class ReferenceDataCodeDto(
  @Schema(description = "Short code for the reference data code", example = "FACIAL_HAIR")
  val domain: String,

  @Schema(description = "Short code for reference data code", example = "FULL_BEARD")
  val code: String,

  @Schema(description = "Description of the reference data code", example = "Full Beard")
  val description: String,

  @Schema(
    description = "The sequence number of the reference data code. " +
      "Used for ordering reference data correctly in lists and dropdowns.",
    example = "3",
  )
  val listSequence: Int,

  @Schema(
    description = "Indicates that the reference data code is active and can be used. " +
      "Inactive reference data codes are not returned by default in the API",
    example = "true",
  )
  val isActive: Boolean,

  @Schema(
    description = "The date and time the reference data code was created",
  )
  val createdAt: ZonedDateTime,

  @Schema(
    description = "The username of the user who created the reference data code",
    example = "USER1234",
  )
  val createdBy: String,

  @Schema(
    description = "The date and time the reference data code was last modified",
  )
  val modifiedAt: ZonedDateTime?,

  @Schema(
    description = "The username of the user who last modified the reference data code",
    example = "USER1234",
  )
  val modifiedBy: String?,

  @Schema(
    description = "The date and time the reference data code was deactivated",
  )
  val deactivatedAt: ZonedDateTime?,

  @Schema(
    description = "The username of the user who deactivated the reference data code",
    example = "USER1234",
  )
  val deactivatedBy: String?,
)
