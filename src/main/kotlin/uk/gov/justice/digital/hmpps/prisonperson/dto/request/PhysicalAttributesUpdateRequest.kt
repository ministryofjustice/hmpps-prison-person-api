package uk.gov.justice.digital.hmpps.prisonperson.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishRange
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishShoeSize
import uk.gov.justice.digital.hmpps.prisonperson.utils.NullishUtils.Companion.getAttribute
import kotlin.reflect.KMutableProperty0

@Schema(
  description = "Request object for updating a prisoner's physical attributes. Can include one or multiple attributes. " +
    "If an attribute is not provided, it will not be updated.  If an attribute is provided and set to 'null' it will be" +
    " updated to equal 'null'.",
)
@JsonInclude(NON_NULL)
data class PhysicalAttributesUpdateRequest(
  @Schema(hidden = true)
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) {
  @Schema(
    description = "Height (in centimetres).",
    example = "180",
    type = "integer",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:NullishRange(
    min = 30,
    max = 274,
    message = "The height must be a plausible value in centimetres (between 30 and 274), null or not provided",
  )
  val height: Nullish<Int> = getAttribute(attributes, "height")

  @Schema(
    description = "Weight (in kilograms).",
    example = "70",
    type = "integer",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:NullishRange(
    min = 12,
    max = 635,
    message = "The weight must be a plausible value in kilograms (between 12 and 635), null or not provided",
  )
  val weight: Nullish<Int> = getAttribute(attributes, "weight")

  @Schema(
    description = "Hair type or colour. `ReferenceDataCode.id`.",
    example = "HAIR_BROWN",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val hair: Nullish<String> = getAttribute(attributes, "hair")

  @Schema(
    description = "Facial hair type. `ReferenceDataCode.id`.",
    example = "FACIAL_HAIR_BEARDED",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val facialHair: Nullish<String> = getAttribute(attributes, "facialHair")

  @Schema(
    description = "Face shape. `ReferenceDataCode.id`.",
    example = "FACE_OVAL",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val face: Nullish<String> = getAttribute(attributes, "face")

  @Schema(
    description = "Build. `ReferenceDataCode.id`.",
    example = "BUILD_MEDIUM",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val build: Nullish<String> = getAttribute(attributes, "build")

  @Schema(
    description = "Left eye colour. `ReferenceDataCode.id`.",
    example = "EYE_GREEN",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val leftEyeColour: Nullish<String> = getAttribute(attributes, "leftEyeColour")

  @Schema(
    description = "Right eye colour. `ReferenceDataCode.id`.",
    example = "EYE_BLUE",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  val rightEyeColour: Nullish<String> = getAttribute(attributes, "rightEyeColour")

  @Schema(
    description = "Shoe size (in UK half sizes).",
    example = "9.5",
    type = "string",
    requiredMode = NOT_REQUIRED,
    nullable = true,
  )
  @field:NullishShoeSize(
    min = "1",
    max = "25",
    message = "The shoe size must be a whole or half size between 1 and 25, null or not provided",
  )
  val shoeSize: Nullish<String> = getAttribute(attributes, "shoeSize")
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
