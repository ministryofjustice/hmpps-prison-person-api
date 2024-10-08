package uk.gov.justice.digital.hmpps.prisonperson.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.info.BuildProperties
import java.util.Properties

class PrisonerHealthInfoTest {
  @Test
  fun `should include version info`() {
    val properties = Properties().apply { setProperty("version", "somever") }
    assertThat(HealthInfo(BuildProperties(properties)).health().details)
      .isEqualTo(mapOf("version" to "somever"))
  }
}
