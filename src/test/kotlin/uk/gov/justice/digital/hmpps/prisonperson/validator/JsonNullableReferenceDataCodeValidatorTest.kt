package uk.gov.justice.digital.hmpps.prisonperson.validator

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.prisonperson.annotation.JsonNullableReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class JsonNullableReferenceDataCodeValidatorTest {

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  lateinit var validator: JsonNullableReferenceDataCodeValidator

  @BeforeEach
  fun setUp() {
    whenever(referenceDataCodeRepository.findById("EXAMPLE_DOMAIN_CODE")).thenReturn(
      Optional.of(
        ReferenceDataCode(
          id = "EXAMPLE_DOMAIN_CODE",
          domain = ReferenceDataDomain(
            code = "EXAMPLE_DOMAIN",
            description = "",
            listSequence = 0,
            createdBy = "",
          ),
          code = "CODE",
          description = "",
          listSequence = 0,
          createdBy = "",
        ),
      ),
    )

    whenever(referenceDataCodeRepository.findById("EXAMPLE_TWO_CODE")).thenReturn(
      Optional.of(
        ReferenceDataCode(
          id = "EXAMPLE_TWO_CODE",
          domain = ReferenceDataDomain(
            code = "OTHER_DOMAIN",
            description = "",
            listSequence = 0,
            createdBy = "",
          ),
          code = "CODE",
          description = "",
          listSequence = 0,
          createdBy = "",
        ),
      ),
    )
  }

  @Test
  fun `valid values (allowNull=true)`() {
    validator.initialize(JsonNullableReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = true))
    assertThat(validator.isValid("EXAMPLE_DOMAIN_CODE", null)).isTrue()
    assertThat(validator.isValid(null, null)).isTrue()
  }

  @Test
  fun `invalid values (allowNull=true)`() {
    validator.initialize(JsonNullableReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = true))
    assertThat(validator.isValid("NON_EXISTING_CODE", null)).isFalse()
    assertThat(validator.isValid("EXAMPLE_TWO_CODE", null)).isFalse()
  }

  @Test
  fun `valid values (allowNull=false)`() {
    validator.initialize(JsonNullableReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = false))
    assertThat(validator.isValid("EXAMPLE_DOMAIN_CODE", null)).isTrue()
  }

  @Test
  fun `invalid values (allowNull=false)`() {
    validator.initialize(JsonNullableReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = true))
    assertThat(validator.isValid("NON_EXISTING_CODE", null)).isFalse()
    assertThat(validator.isValid("EXAMPLE_TWO_CODE", null)).isFalse()
    assertThat(validator.isValid(null, null)).isTrue()
  }
}
