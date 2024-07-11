package uk.gov.justice.digital.hmpps.prisonperson.dto

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import java.time.ZonedDateTime

@Schema(description = "Reference Data Domain")
data class ReferenceDataDomainDto(
  @Schema(description = "Short code for the reference data domain", example = "HAIR")
  val code: String,

  @Schema(description = "Description of the reference data domain", example = "Hair type or colour")
  val description: String,

  @Schema(
    description = "The sequence number of the reference data domain. Used for ordering domains correctly in lists.",
    example = "3",
  )
  val listSequence: Int,

  @Schema(
    description = "Indicates that the reference data domain is active and can be used. " +
      "Inactive reference data domains are not returned by default in the API",
    example = "true",
  )
  val isActive: Boolean,

  @Schema(
    description = "The date and time the reference data domain was created",
    example = "2024-06-30T15:43:27+0000",
  )
  val createdAt: ZonedDateTime,

  @Schema(
    description = "The username of the user who created the reference data domain",
    example = "USER1234",
  )
  val createdBy: String,

  @Schema(
    description = "The date and time the reference data domain was last modified",
    example = "2024-07-01T09:41:00+0000",
  )
  val modifiedAt: ZonedDateTime?,

  @Schema(
    description = "The username of the user who last modified the reference data domain",
    example = "USER1234",
  )
  val modifiedBy: String?,

  @Schema(
    description = "The date and time the reference data domain was deactivated",
    example = "2024-07-08T22:30:11+0000",
  )
  val deactivatedAt: ZonedDateTime?,

  @Schema(
    description = "The username of the user who deactivated the reference data domain",
    example = "USER1234",
  )
  val deactivatedBy: String?,

  @Schema(
    description = "The reference data codes associated with this reference data domain",
  )
  val referenceDataCodes: Collection<ReferenceDataCode>,
)
