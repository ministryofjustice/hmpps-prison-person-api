package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import java.time.ZonedDateTime

@Schema(description = "Field History")
@JsonInclude(NON_NULL)
data class FieldHistoryDto @JsonCreator constructor(
  @Schema(description = "Prisoner Number", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "Field name", example = "HEIGHT")
  val field: PrisonPersonField,

  @Schema(description = "Integer value of the field in the date range `appliesFrom` to `appliesTo`", example = "1")
  val valueInt: Int? = null,

  @Schema(description = "String value of the field in the date range `appliesFrom` to `appliesTo`", example = "string")
  val valueString: String? = null,

  @Schema(description = "Reference Data Code value of the field in the date range `appliesFrom` to `appliesTo`")
  val valueRef: ReferenceDataSimpleDto? = null,

  @Schema(description = "The timestamp the field value applies from")
  val appliesFrom: ZonedDateTime = ZonedDateTime.now(),

  @Schema(description = "The timestamp the field value applies up to. If null, the value currently applies.")
  val appliesTo: ZonedDateTime? = null,

  @Schema(description = "The date and time the field history entry was created")
  val createdAt: ZonedDateTime = ZonedDateTime.now(),

  @Schema(description = "The username of the user who created the field history entry")
  val createdBy: String,

  @Schema(description = "The date and time the field history entry was migrated")
  val migratedAt: ZonedDateTime? = null,

  @Schema(description = "The date and time the field history entry was merged")
  val mergedAt: ZonedDateTime? = null,

  @Schema(description = "The Prisoner Number the field history entry was merged from")
  val mergedFrom: String? = null,

  @Schema(description = "The source of the change, either DPS or NOMIS. Will be NOMIS if the record was either migrated or synced from NOMIS")
  val source: String? = null,

  @Schema(description = "A flag to indicate that when the data was migrated from NOMIS, the booking it was taken from was historical and the booking start and/or end dates did not align sequentially into the timeline of other bookings for the same prisoner")
  val anomalous: Boolean,
)
