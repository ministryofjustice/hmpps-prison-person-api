package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Schema(
  description = "Request object for updating a prisoner's health information. Can include one or multiple fields. " +
    "If an attribute is not defined in the map, it will be set as Undefined.",
)
@JsonInclude(NON_NULL)
data class HealthUpdateRequest(
  @Schema(description = "Map of attributes")
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) : MutableMap<String, Any?> by attributes {
  @Schema(description = "Smoker or vaper. `ReferenceDataCode`.`id`.", example = "SMOKE_NO")
  val smokerOrVaper: Nullish<String> = getAttribute("smokerOrVaper")

  /**
   * Get an attribute from the map of `attributes` and push it into a `Nullish` object
   *
   * @param name the name of the attribute to get
   */
  private inline fun <reified T> getAttribute(name: String): Nullish<T> {
    if (!containsKey(name)) {
      @Suppress("UNCHECKED_CAST")
      return Nullish.Undefined as Nullish<T>
    }

    val value = this[name]
    if (value is T || value == null) {
      return Nullish.Defined(value as? T)
    } else {
      throw IllegalArgumentException("$name is not an instance of ${T::class.java.simpleName}")
    }
  }
}
