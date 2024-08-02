package uk.gov.justice.digital.hmpps.prisonperson.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class ReferenceDataDomainMapperTest {

  private val testDomain = ReferenceDataDomain("HAIR", "Description", 1, ZonedDateTime.now(), "testUser")
  private val testCode = ReferenceDataCode(
    id = "${testDomain}_MOUSE",
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

    assertThat(dto.code).isEqualTo("HAIR")
    assertThat(dto.description).isEqualTo("Description")
    assertThat(dto.referenceDataCodes.size).isEqualTo(1)
    assertThat(dto.referenceDataCodes.first().code).isEqualTo("MOUSE")
    assertThat(dto.referenceDataCodes.first().description).isEqualTo("Mousy")
  }

  @Test
  fun `test isActive when deactivatedAt is null`() {
    val referenceDataDomain = testCode

    assertThat(referenceDataDomain.isActive()).isEqualTo(true)
  }

  @Test
  fun `test isActive when deactivatedAt is in the future`() {
    val referenceDataDomain = testCode
    referenceDataDomain.lastModifiedAt = ZonedDateTime.now()
    referenceDataDomain.lastModifiedBy = "testUser"
    referenceDataDomain.deactivatedAt = ZonedDateTime.now().plusDays(1)
    referenceDataDomain.deactivatedBy = "testUser"

    assertThat(referenceDataDomain.isActive()).isEqualTo(true)
  }

  @Test
  fun `test isActive when deactivatedAt is in the past`() {
    val referenceDataDomain = testCode
    referenceDataDomain.lastModifiedAt = ZonedDateTime.now()
    referenceDataDomain.lastModifiedBy = "testUser"
    referenceDataDomain.deactivatedAt = ZonedDateTime.now().minusDays(1)
    referenceDataDomain.deactivatedBy = "testUser"

    assertThat(referenceDataDomain.isActive()).isEqualTo(false)
  }
}
