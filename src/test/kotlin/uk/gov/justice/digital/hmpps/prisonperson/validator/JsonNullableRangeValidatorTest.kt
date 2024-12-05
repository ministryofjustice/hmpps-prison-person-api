package uk.gov.justice.digital.hmpps.prisonperson.validator

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableRange

class JsonNullableRangeValidatorTest {

  private lateinit var validator: JsonNullableRangeValidator

  @BeforeEach
  fun setUp() {
    validator = JsonNullableRangeValidator()
    validator.initialize(JsonNullableRange(min = 5, max = 10))
  }

  @Test
  fun `valid values`() {
    assertThat(validator.isValid(null, null)).isTrue()
    assertThat(validator.isValid(5, null)).isTrue()
    assertThat(validator.isValid(10, null)).isTrue()
    assertThat(validator.isValid(7, null)).isTrue()
  }

  @Test
  fun `invalid values`() {
    assertThat(validator.isValid(4, null)).isFalse()
    assertThat(validator.isValid(11, null)).isFalse()
    assertThat(validator.isValid(-1, null)).isFalse()
    assertThat(validator.isValid(0, null)).isFalse()
  }
}
