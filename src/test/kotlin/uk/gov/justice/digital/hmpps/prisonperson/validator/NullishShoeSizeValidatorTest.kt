package uk.gov.justice.digital.hmpps.prisonperson.validator

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.annotation.NullishShoeSize
import uk.gov.justice.digital.hmpps.prisonperson.utils.Nullish.Defined
import uk.gov.justice.digital.hmpps.prisonperson.utils.Nullish.Undefined

class NullishShoeSizeValidatorTest {

  private lateinit var validator: NullishShoeSizeValidator

  @BeforeEach
  fun setUp() {
    validator = NullishShoeSizeValidator()
    validator.initialize(NullishShoeSize(min = "1", max = "25"))
  }

  @Test
  fun `valid shoe sizes`() {
    assertThat(validator.isValid(Undefined, null)).isTrue()
    assertThat(validator.isValid(Defined(null), null)).isTrue()
    assertThat(validator.isValid(Defined("1"), null)).isTrue()
    assertThat(validator.isValid(Defined("1.0"), null)).isTrue()
    assertThat(validator.isValid(Defined("25"), null)).isTrue()
    assertThat(validator.isValid(Defined("25.0"), null)).isTrue()
    assertThat(validator.isValid(Defined("9"), null)).isTrue()
    assertThat(validator.isValid(Defined("10.0"), null)).isTrue()
    assertThat(validator.isValid(Defined("11.5"), null)).isTrue()
    assertThat(validator.isValid(Defined("24.5"), null)).isTrue()
  }

  @Test
  fun `invalid shoe sizes`() {
    assertThat(validator.isValid(Defined("0"), null)).isFalse()
    assertThat(validator.isValid(Defined("0.5"), null)).isFalse()
    assertThat(validator.isValid(Defined("25.5"), null)).isFalse()
    assertThat(validator.isValid(Defined("26"), null)).isFalse()
    assertThat(validator.isValid(Defined("5.3"), null)).isFalse()
    assertThat(validator.isValid(Defined("12.6"), null)).isFalse()
    assertThat(validator.isValid(Defined("One.5"), null)).isFalse()
    assertThat(validator.isValid(Defined("Five"), null)).isFalse()
  }
}
