package uk.gov.justice.digital.hmpps.prisonperson.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import java.time.ZonedDateTime

@Schema(description = "Field History")
@JsonInclude(NON_NULL)
data class FieldHistoryDto(
  @Schema(description = "Prisoner Number", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "Field name", example = "HEIGHT")
  val field: PrisonPersonField,

  @Schema(description = "Integer value of the field in the date range `appliesFrom` to `appliesTo`", example = "1")
  val valueInt: Int? = null,

  @Schema(description = "String value of the field in the date range `appliesFrom` to `appliesTo`", example = "string")
  val valueString: String? = null,

  @Schema(description = "Reference Data Code value of the field in the date range `appliesFrom` to `appliesTo`")
  val valueRef: ReferenceDataCodeDto? = null,

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
)
