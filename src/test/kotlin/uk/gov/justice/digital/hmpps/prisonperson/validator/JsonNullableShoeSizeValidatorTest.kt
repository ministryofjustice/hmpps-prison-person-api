package uk.gov.justice.digital.hmpps.prisonperson.validator

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableShoeSize

class JsonNullableShoeSizeValidatorTest {

  private lateinit var validator: JsonNullableShoeSizeValidator

  @BeforeEach
  fun setUp() {
    validator = JsonNullableShoeSizeValidator()
    validator.initialize(JsonNullableShoeSize(min = "1", max = "25"))
  }

  @Test
  fun `valid shoe sizes`() {
    assertThat(validator.isValid(null, null)).isTrue()
    assertThat(validator.isValid("1", null)).isTrue()
    assertThat(validator.isValid("1.0", null)).isTrue()
    assertThat(validator.isValid("25", null)).isTrue()
    assertThat(validator.isValid("25.0", null)).isTrue()
    assertThat(validator.isValid("9", null)).isTrue()
    assertThat(validator.isValid("10.0", null)).isTrue()
    assertThat(validator.isValid("11.5", null)).isTrue()
    assertThat(validator.isValid("24.5", null)).isTrue()
  }

  @Test
  fun `invalid shoe sizes`() {
    assertThat(validator.isValid("0", null)).isFalse()
    assertThat(validator.isValid("0.5", null)).isFalse()
    assertThat(validator.isValid("25.5", null)).isFalse()
    assertThat(validator.isValid("26", null)).isFalse()
    assertThat(validator.isValid("5.3", null)).isFalse()
    assertThat(validator.isValid("12.6", null)).isFalse()
    assertThat(validator.isValid("One.5", null)).isFalse()
    assertThat(validator.isValid("Five", null)).isFalse()
  }
}
