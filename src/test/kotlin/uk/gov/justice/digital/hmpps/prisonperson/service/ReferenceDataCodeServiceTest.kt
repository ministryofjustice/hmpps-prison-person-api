package uk.gov.justice.digital.hmpps.prisonperson.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.config.ReferenceDataCodeNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class ReferenceDataCodeServiceTest {

  @Mock
  private lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  private lateinit var referenceDataCodeService: ReferenceDataCodeService

  @Test
  fun `test getReferenceDataCodes with includeInactive`() {
    val activeCode = ACTIVE_CODE
    val inactiveCode = INACTIVE_CODE
    whenever(referenceDataCodeRepository.findAllByDomainAndIncludeInactive(DOMAIN.code, true)).thenReturn(
      listOf(
        activeCode,
        inactiveCode,
      ),
    )

    val result = referenceDataCodeService.getReferenceDataCodes(DOMAIN.code, true)

    assertEquals(2, result.size)
    verify(referenceDataCodeRepository).findAllByDomainAndIncludeInactive(DOMAIN.code, true)
  }

  @Test
  fun `test getReferenceDataCodes without includeInactive`() {
    val activeCode = ACTIVE_CODE
    whenever(referenceDataCodeRepository.findAllByDomainAndIncludeInactive(DOMAIN.code, false)).thenReturn(
      listOf(
        activeCode,
      ),
    )

    val result = referenceDataCodeService.getReferenceDataCodes(DOMAIN.code, false)

    assertEquals(1, result.size)
    verify(referenceDataCodeRepository).findAllByDomainAndIncludeInactive(DOMAIN.code, false)
  }

  @Test
  fun `test getReferenceDataCode found`() {
    val code = "ACTIVE"
    val referenceDataCode = ACTIVE_CODE

    whenever(referenceDataCodeRepository.findByCodeAndDomainCode(code, DOMAIN.code)).thenReturn(
      referenceDataCode,
    )

    val result = referenceDataCodeService.getReferenceDataCode(code, DOMAIN.code)

    assertNotNull(result)
    assertEquals(code, result.code)
    verify(referenceDataCodeRepository).findByCodeAndDomainCode(code, DOMAIN.code)
  }

  @Test
  fun `test getReferenceDataCode not found`() {
    val code = "NONEXISTENT"
    whenever(referenceDataCodeRepository.findByCodeAndDomainCode(code, DOMAIN.code)).thenReturn(null)

    val exception = assertThrows(ReferenceDataCodeNotFoundException::class.java) {
      referenceDataCodeService.getReferenceDataCode(code, DOMAIN.code)
    }

    assertEquals("No data for code 'NONEXISTENT' in domain 'DOMAIN'", exception.message)
    verify(referenceDataCodeRepository).findByCodeAndDomainCode(code, DOMAIN.code)
  }

  private companion object {
    val DOMAIN = ReferenceDataDomain("DOMAIN", "Domain", 0, ZonedDateTime.now(), "testUser")
    val ACTIVE_CODE = ReferenceDataCode("ACTIVE", DOMAIN, "Active domain", 0, ZonedDateTime.now(), "testUser")
    val INACTIVE_CODE =
      ReferenceDataCode("INACTIVE", DOMAIN, "Inactive domain", 0, ZonedDateTime.now(), "testUser").apply {
        this.deactivatedAt = ZonedDateTime.now()
        this.deactivatedBy = "testUser"
      }
  }
}