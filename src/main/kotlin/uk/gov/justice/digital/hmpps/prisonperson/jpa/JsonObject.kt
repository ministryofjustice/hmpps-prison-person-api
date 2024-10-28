package uk.gov.justice.digital.hmpps.prisonperson.jpa

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField

data class JsonObject(
  val field: PrisonPersonField,

  @JsonTypeInfo(use = NAME, property = "field", include = EXTERNAL_PROPERTY)
  @JsonSubTypes(
    JsonSubTypes.Type(value = FoodAllergies::class, name = "FOOD_ALLERGY"),
    JsonSubTypes.Type(value = MedicalDietaryRequirements::class, name = "MEDICAL_DIET"),
  )
  val value: Any?,
)
