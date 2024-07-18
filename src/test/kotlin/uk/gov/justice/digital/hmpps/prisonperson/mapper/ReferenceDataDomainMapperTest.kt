package uk.gov.justice.digital.hmpps.prisonperson.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class ReferenceDataDomainMapperTest {

  private val testDomain = ReferenceDataDomain("HAIR", "Description", 1, ZonedDateTime.now(), "testUser")
  private val testCode = ReferenceDataCode(
    domain = testDomain,
    code = "MOUSE",
    description = "Default Description",
    listSequence = 1,
    createdAt = ZonedDateTime.now(),
    createdBy = "testUser",
  )

  @Test
  fun `test toDto`() {
    val referenceDataDomain = testDomain
    testDomain.referenceDataCodes = mutableListOf(testCode)

    val dto = referenceDataDomain.toDto()

    assertEquals("HAIR", dto.code)
    assertEquals("Description", dto.description)
    assertEquals(1, dto.referenceDataCodes.size)
    assertEquals("MOUSE", dto.referenceDataCodes.first().code)
    assertEquals("Mousy", dto.referenceDataCodes.first().description)
  }

  @Test
  fun `test isActive when deactivatedAt is null`() {
    val referenceDataDomain = testCode

    assertEquals(true, referenceDataDomain.isActive())
  }

  @Test
  fun `test isActive when deactivatedAt is in the future`() {
    val referenceDataDomain = testCode
    referenceDataDomain.lastModifiedAt = ZonedDateTime.now()
    referenceDataDomain.lastModifiedBy = "testUser"
    referenceDataDomain.deactivatedAt = ZonedDateTime.now().plusDays(1)
    referenceDataDomain.deactivatedBy = "testUser"

    assertEquals(true, referenceDataDomain.isActive())
  }

  @Test
  fun `test isActive when deactivatedAt is in the past`() {
    val referenceDataDomain = testCode
    referenceDataDomain.lastModifiedAt = ZonedDateTime.now()
    referenceDataDomain.lastModifiedBy = "testUser"
    referenceDataDomain.deactivatedAt = ZonedDateTime.now().minusDays(1)
    referenceDataDomain.deactivatedBy = "testUser"

    assertEquals(false, referenceDataDomain.isActive())
  }
}
