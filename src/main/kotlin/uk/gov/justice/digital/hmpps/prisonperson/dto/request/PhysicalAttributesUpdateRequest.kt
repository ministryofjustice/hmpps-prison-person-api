package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishRange
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishShoeSize
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
    message = "The height must be a plausible value in centimetres (between 30 and 274), null or Undefined",
  )
  val height: Nullish<Int> = getAttribute("height")

  @Schema(description = "Weight (in kilograms).", example = "70")
  @field:NullishRange(
    min = 12,
    max = 635,
    message = "The weight must be a plausible value in kilograms (between 12 and 635), null or Undefined",
  )
  val weight: Nullish<Int> = getAttribute("weight")

  @Schema(description = "Hair type or colour. ReferenceDataCode `id`.", example = "HAIR_BROWN")
  val hair: Nullish<String> = getAttribute("hair")

  @Schema(
    type = "Nullish",
    required = false,
    description = "Facial hair type. ReferenceDataCode `id`.",
    example = "FACIAL_HAIR_BEARDED",
  )
  val facialHair: Nullish<String> = getAttribute("facialHair")

  @Schema(description = "Face shape. `ReferenceDataCode`.`id`.", example = "FACE_OVAL")
  val face: Nullish<String> = getAttribute("face")

  @Schema(description = "Build. `ReferenceDataCode.id`.", example = "BUILD_MEDIUM")
  val build: Nullish<String> = getAttribute("build")

  @Schema(description = "Left eye colour. `ReferenceDataCode.id`.", example = "EYE_GREEN")
  val leftEyeColour: Nullish<String> = getAttribute("leftEyeColour")

  @Schema(description = "Right eye colour. `ReferenceDataCode.id`.", example = "EYE_BLUE")
  val rightEyeColour: Nullish<String> = getAttribute("rightEyeColour")

  @Schema(description = "Shoe size (in UK half sizes).", example = "9.5")
  @field:NullishShoeSize(
    min = "1",
    max = "25",
    message = "The shoe size must be a whole or half size between 1 and 25, null or Undefined",
  )
  val shoeSize: Nullish<String> = getAttribute("shoeSize")

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

/**
 * Nullish sealed interface to define a type that can have a **Defined** `value` including `null`, or be **Undefined**
 */
sealed interface Nullish<T> {
  data object Undefined : Nullish<Nothing>
  data class Defined<T>(val value: T?) : Nullish<T>

  /**
   * `apply` the value of the Nullish object to the nullable property `prop`
   *
   * If the Nullish object is **Undefined**, do nothing
   *
   * Otherwise, set the **Defined** `value` of the Nullish on the `prop` even if the `value` is `null`
   *
   * @param prop the nullable property to set to the **Defined** `value`
   */
  fun apply(prop: KMutableProperty0<T?>) {
    when (this) {
      Undefined -> return
      is Defined -> prop.set(value)
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
  fun <U> apply(prop: KMutableProperty0<U?>, fn: (T) -> U?) {
    when (this) {
      is Undefined -> return
      is Defined -> {
        val currentValue = value
        val modifiedValue = currentValue?.let { fn(it) }
        prop.set(modifiedValue)
      }
    }
  }
}
