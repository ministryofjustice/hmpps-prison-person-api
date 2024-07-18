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
import uk.gov.justice.digital.hmpps.prisonperson.config.ReferenceDataDomainNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataDomainRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class ReferenceDataDomainServiceTest {

  @Mock
  private lateinit var referenceDataDomainRepository: ReferenceDataDomainRepository

  @InjectMocks
  private lateinit var referenceDataDomainService: ReferenceDataDomainService

  @Test
  fun `test getReferenceDataDomains with includeInactive`() {
    val activeDomain = ACTIVE_DOMAIN
    val inactiveDomain = INACTIVE_DOMAIN
    whenever(referenceDataDomainRepository.findAllByIncludeInactive(true)).thenReturn(
      listOf(
        activeDomain,
        inactiveDomain,
      ),
    )

    val result = referenceDataDomainService.getReferenceDataDomains(true)

    assertEquals(2, result.size)
    verify(referenceDataDomainRepository).findAllByIncludeInactive(true)
  }

  @Test
  fun `test getReferenceDataDomains without includeInactive`() {
    val activeDomain = ACTIVE_DOMAIN
    whenever(referenceDataDomainRepository.findAllByIncludeInactive(false)).thenReturn(listOf(activeDomain))

    val result = referenceDataDomainService.getReferenceDataDomains(false)

    assertEquals(1, result.size)
    verify(referenceDataDomainRepository).findAllByIncludeInactive(false)
  }

  @Test
  fun `test getReferenceDataDomain found`() {
    val code = "ACTIVE"
    val referenceDataDomain = ACTIVE_DOMAIN

    whenever(referenceDataDomainRepository.findByCode(code)).thenReturn(referenceDataDomain)

    val result = referenceDataDomainService.getReferenceDataDomain(code)

    assertNotNull(result)
    assertEquals(code, result.code)
    verify(referenceDataDomainRepository).findByCode(code)
  }

  @Test
  fun `test getReferenceDataDomain not found`() {
    val code = "NONEXISTENT"
    whenever(referenceDataDomainRepository.findByCode(code)).thenReturn(null)

    val exception = assertThrows(ReferenceDataDomainNotFoundException::class.java) {
      referenceDataDomainService.getReferenceDataDomain(code)
    }

    assertEquals("No data for domain 'NONEXISTENT'", exception.message)
    verify(referenceDataDomainRepository).findByCode(code)
  }

  private companion object {
    val ACTIVE_DOMAIN = ReferenceDataDomain("ACTIVE", "Active domain", 0, ZonedDateTime.now(), "testUser")
    val INACTIVE_DOMAIN = ReferenceDataDomain("INACTIVE", "Inactive domain", 0, ZonedDateTime.now(), "testUser").apply {
      this.deactivatedAt = ZonedDateTime.now()
      this.deactivatedBy = "testUser"
    }
  }
}
