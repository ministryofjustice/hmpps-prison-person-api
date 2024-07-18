package uk.gov.justice.digital.hmpps.prisonperson.mapper

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class ReferenceDataCodeMapperTest {

  @BeforeEach
  fun setUp() {
    mockkObject(ReferenceDataCodeMapper)
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  private val testDomain = ReferenceDataDomain("TEST", "Test domain", 1, ZonedDateTime.now(), "testUser")
  private val testCode = ReferenceDataCode(
    domain = testDomain,
    code = "ORANGE",
    description = "Orange",
    listSequence = 1,
    createdAt = ZonedDateTime.now(),
    createdBy = "testUser",
  )

  @Test
  fun `test toDto with description mapping`() {
    val referenceDataCode = testCode

    every { ReferenceDataCodeMapper.referenceDataCodeDescriptionMappings } returns
      mapOf(
        "TEST" to mapOf(
          "ORANGE" to "Purple",
        ),
      )

    val dto = referenceDataCode.toDto()

    assertEquals("TEST", dto.domain)
    assertEquals("ORANGE", dto.code)
    assertEquals("Purple", dto.description) // Should map to "Purple"
  }

  @Test
  fun `test toDto with default description`() {
    val referenceDataCode = ReferenceDataCode(
      domain = testDomain,
      code = "ORANGE",
      description = "Orange",
      listSequence = 1,
      createdAt = ZonedDateTime.now(),
      createdBy = "testUser",
    )

    val dto = referenceDataCode.toDto()

    assertEquals("TEST", dto.domain)
    assertEquals("ORANGE", dto.code)
    assertEquals("Orange", dto.description) // Should use default description
  }

  @Test
  fun `test isActive when deactivatedAt is null`() {
    val referenceDataCode = testCode

    assertEquals(true, referenceDataCode.isActive())
  }

  @Test
  fun `test isActive when deactivatedAt is in the future`() {
    val referenceDataCode = testCode
    referenceDataCode.lastModifiedAt = ZonedDateTime.now()
    referenceDataCode.lastModifiedBy = "testUser"
    referenceDataCode.deactivatedAt = ZonedDateTime.now().plusDays(1)
    referenceDataCode.deactivatedBy = "testUser"

    assertEquals(true, referenceDataCode.isActive())
  }

  @Test
  fun `test isActive when deactivatedAt is in the past`() {
    val referenceDataCode = testCode
    referenceDataCode.lastModifiedAt = ZonedDateTime.now()
    referenceDataCode.lastModifiedBy = "testUser"
    referenceDataCode.deactivatedAt = ZonedDateTime.now().minusDays(1)
    referenceDataCode.deactivatedBy = "testUser"

    assertEquals(false, referenceDataCode.isActive())
  }
}
