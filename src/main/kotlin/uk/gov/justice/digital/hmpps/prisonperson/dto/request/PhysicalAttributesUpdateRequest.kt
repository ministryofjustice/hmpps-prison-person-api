package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishRange
import kotlin.reflect.KMutableProperty0

@Schema(
  description = "Request object for updating a prisoner's physical attributes. Can include one or multiple attributes. " +
    "If an attribute is not defined in the map, it will be set as Undefined.",
)
@JsonInclude(NON_NULL)
data class PhysicalAttributesUpdateRequest(

  @Schema(description = "Map of attributes")
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),

) : MutableMap<String, Any?> by attributes {

  @Schema(description = "Height (in centimetres).", example = "180")
  @field:NullishRange(
    min = 30,
    max = 274,
    message = "The height must be a plausible value in centimetres (between 30 and 274)",
  )
  val height: Nullish = getAttribute("height")

  @Schema(description = "Weight (in kilograms).", example = "70")
  @field:NullishRange(
    min = 12,
    max = 635,
    message = "The weight must be a plausible value in kilograms (between 12 and 635)",
  )
  val weight: Nullish = getAttribute("weight")

  @Schema(description = "Hair type or colour. ReferenceDataCode `id`.", example = "HAIR_BROWN")
  val hair: Nullish = getAttribute("hair")

  @Schema(
    type = "Nullish",
    required = false,
    description = "Facial hair type. ReferenceDataCode `id`.",
    example = "FACIAL_HAIR_BEARDED",
  )
  val facialHair: Nullish = getAttribute("facialHair")

  @Schema(description = "Face shape. `ReferenceDataCode`.`id`.", example = "FACE_OVAL")
  val face: Nullish = getAttribute("face")

  @Schema(description = "Build. `ReferenceDataCode.id`.", example = "BUILD_MEDIUM")
  val build: Nullish = getAttribute("build")

  /**
   * Get an attribute from the map of `attributes` and push it into a `Nullish` object
   *
   * @param name the name of the attribute to get
   */
  private fun getAttribute(name: String): Nullish = if (containsKey(name)) {
    Nullish.Defined(this[name])
  } else {
    Nullish.Undefined
  }
}

/**
 * Nullish sealed interface to define a type that can have a **Defined** `value` including `null`, or be **Undefined**
 */
sealed interface Nullish {
  data object Undefined : Nullish
  data class Defined(val value: Any?) : Nullish

  /**
   * `apply` the value of the Nullish object to the nullable property `prop`
   *
   * If the Nullish object is **Undefined**, do nothing
   *
   * Otherwise, set the **Defined** `value` of the Nullish on the `prop` even if the `value` is `null`
   *
   * @param prop the nullable property to set to the **Defined** `value`
   */
  fun <T> apply(prop: KMutableProperty0<T?>) {
    @Suppress("UNCHECKED_CAST")
    when (this) {
      Undefined -> return
      is Defined -> prop.set(value as T?)
    }
  }

  /**
   * `apply` the value of the Nullish object to the nullable property `prop` after mapping via `fn`
   *
   * If the Nullish object is **Undefined**, do nothing
   *
   * Otherwise, map the **Defined** `value` of the Nullish using `fn` and set the mapped value
   * on the `prop` even if the `value` is `null`
   *
   * @param prop the nullable property to set to a mapped version of the **Defined** `value`
   * @param fn a function to map the `value` before setting on the `prop`
   */
  fun <T, U> apply(prop: KMutableProperty0<U?>, fn: (T) -> U?) {
    when (this) {
      is Undefined -> return
      is Defined -> {
        @Suppress("UNCHECKED_CAST")
        val currentValue = value as? T
        val modifiedValue = currentValue?.let { fn(it) }
        prop.set(modifiedValue)
      }
    }
  }
}
