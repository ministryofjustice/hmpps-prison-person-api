package uk.gov.justice.digital.hmpps.prisonperson.utils

import uk.gov.justice.digital.hmpps.prisonperson.dto.request.Nullish

class NullishUtils {
  companion object {
    /**
     * Get an attribute from the map of `attributes` and push it into a `Nullish` object
     *
     * @param name the name of the attribute to get
     */
    inline fun <reified T> getAttribute(map: MutableMap<String, Any?>, name: String): Nullish<T> {
      if (!map.containsKey(name)) {
        @Suppress("UNCHECKED_CAST")
        return Nullish.Undefined as Nullish<T>
      }

      val value = map[name]
      if (value is T || value == null) {
        return Nullish.Defined(value as? T)
      } else {
        throw IllegalArgumentException("$name is not an instance of ${T::class.java.simpleName}")
      }
    }
  }
}
