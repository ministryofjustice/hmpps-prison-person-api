package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesSyncRequest
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class PhysicalAttributesSyncServiceTest {

  @InjectMocks
  lateinit var underTest: PhysicalAttributesSyncService

  @Nested
  inner class UpdatePhysicalAttributes {
    @Test
    fun `not yet implemented`() {
      assertThatThrownBy {
        underTest.sync(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_SYNC_REQUEST)
      }.isInstanceOf(NotImplementedError::class.java)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70

    val NOW: ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/London"))

    val PHYSICAL_ATTRIBUTES_SYNC_REQUEST = PhysicalAttributesSyncRequest(PRISONER_HEIGHT, PRISONER_WEIGHT, appliesFrom = NOW, createdAt = NOW)
  }
}
