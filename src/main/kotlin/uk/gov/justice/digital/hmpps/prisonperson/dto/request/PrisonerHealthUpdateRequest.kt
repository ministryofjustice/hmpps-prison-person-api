package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import jakarta.validation.constraints.NotNull
import org.openapitools.jackson.nullable.JsonNullable
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableReferenceDataCode

const val REF_DATA_LIST_ERROR = "The value must be a a list of domain codes of the correct domain, an empty list, or Undefined."

@Schema(
  description = "Request object for updating a prisoner's health information. Can include one or multiple fields. " +
    "If an attribute is provided and set to 'null' it will be updated equal to 'null'. " +
    "If it is not provided it is not updated",
)
@JsonInclude(NON_NULL)
data class PrisonerHealthUpdateRequest(
  @Schema(
    description = "Smoker or vaper. `ReferenceDataCode`.`id`.",
    type = "string",
    example = "SMOKE_NO",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:JsonNullableReferenceDataCode(
    domains = ["SMOKE"],
    allowNull = true,
  )
  val smokerOrVaper: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(
    description = "The food allergies the prisoner has. A list of `ReferenceDataCode`.`id`",
    type = "string[]",
    example = "[FOOD_ALLERGY_EGG, FOOD_ALLERGY_MILK]",
    requiredMode = NOT_REQUIRED,
    nullable = false,
  )
  @field:NotNull(message = REF_DATA_LIST_ERROR)
  val foodAllergies: JsonNullable<
    List<
      @JsonNullableReferenceDataCode(
        domains = ["FOOD_ALLERGY"],
        allowNull = false,
        message = REF_DATA_LIST_ERROR,
      )
      String,
      >,
    > = JsonNullable.undefined(),

  @Schema(
    description = "The medical dietary requirements the prisoner has. A list of `ReferenceDataCode`.`id`",
    type = "string[]",
    example = "[MEDICAL_DIET_LOW_FAT, FREE_FROM_EGG]",
    requiredMode = NOT_REQUIRED,
    nullable = false,
  )
  @field:NotNull(message = REF_DATA_LIST_ERROR)
  val medicalDietaryRequirements: JsonNullable<
    List<
      @JsonNullableReferenceDataCode(
        domains = ["MEDICAL_DIET", "FREE_FROM"],
        allowNull = false,
        message = REF_DATA_LIST_ERROR,
      )
      String,
      >,
    > = JsonNullable.undefined(),
)
