package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.HealthUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.Health
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.HealthRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toDto
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class HealthServiceTest {
  @Mock
  lateinit var healthRepository: HealthRepository

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  lateinit var underTest: HealthService

  private val savedHealth = argumentCaptor<Health>()

  @Test
  fun `prison person data not found`() {
    whenever(healthRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

    val result = underTest.getHealth(PRISONER_NUMBER)
    assertThat(result).isNull()
  }

  @Test
  fun `prison health data is found`() {
    whenever(healthRepository.findById(PRISONER_NUMBER)).thenReturn(
      Optional.of(
        Health(
          prisonerNumber = PRISONER_NUMBER,
          smokerOrVaper = SMOKER_OR_VAPER,
        ),
      ),
    )

    val result = underTest.getHealth(PRISONER_NUMBER)
    assertThat(result).isEqualTo(
      HealthDto(
        smokerOrVaper = ReferenceDataCodeDto(
          id = REFERENCE_DATA_CODE_ID,
          domain = REFERENCE_DATA_DOMAIN_CODE,
          code = REFERENCE_DATA_CODE,
          createdBy = USER1,
          createdAt = NOW,
          description = REFERENCE_DATA_CODE_DESCRPTION,
          listSequence = REFERENCE_DATA_LIST_SEQUENCE,
          deactivatedAt = null,
          deactivatedBy = null,
          lastModifiedBy = null,
          lastModifiedAt = null,
          isActive = true,
        ),
      ),
    )
  }

  @Nested
  inner class CreateOrUpdateHealth {

    @BeforeEach
    fun beforeEach() {
      whenever(healthRepository.save(savedHealth.capture())).thenAnswer { savedHealth.firstValue }
      whenever(referenceDataCodeRepository.findById(REFERENCE_DATA_CODE_ID)).thenReturn(Optional.of(SMOKER_OR_VAPER))
    }

    @Test
    fun `creating new health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PRISONER_SEARCH_RESPONSE,
      )

      whenever(healthRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(
        underTest.createOrUpdate(
          PRISONER_NUMBER,
          HEALTH_UPDATE_REQUEST,
        ),
      ).isEqualTo(
        HealthDto(
          SMOKER_OR_VAPER.toDto(),
        ),
      )
    }

    @Test
    fun `updating health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PRISONER_SEARCH_RESPONSE,
      )

      whenever(healthRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          Health(
            prisonerNumber = PRISONER_NUMBER,
            smokerOrVaper = SMOKER_OR_VAPER,
          ),
        ),
      )

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, HEALTH_UPDATE_REQUEST_WITH_NULL))
        .isEqualTo(HealthDto(null))
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    val NOW = ZonedDateTime.now()

    val REFERENCE_DATA_CODE_ID = "EXAMPLE_CODE"
    val REFERENCE_DATA_CODE = "CODE"
    val REFERENCE_DATA_CODE_DESCRPTION = "Example code"
    val REFERENCE_DATA_LIST_SEQUENCE = 0
    val REFERENCE_DATA_DOMAIN_CODE = "EXAMPLE"
    val REFERENCE_DATA_DOMAIN_DESCRIPTION = "Example"

    val attributes = mutableMapOf<String, Any?>(
      Pair("smokerOrVaper", REFERENCE_DATA_CODE_ID),
    )
    val HEALTH_UPDATE_REQUEST = HealthUpdateRequest(attributes)

    val attributes_undefined = mutableMapOf<String, Any?>(
      Pair("smokerOrVaper", null),
    )
    val HEALTH_UPDATE_REQUEST_WITH_NULL = HealthUpdateRequest(attributes_undefined)

    val SMOKER_OR_VAPER = ReferenceDataCode(
      id = REFERENCE_DATA_CODE_ID,
      code = REFERENCE_DATA_CODE,
      createdBy = USER1,
      createdAt = NOW,
      description = REFERENCE_DATA_CODE_DESCRPTION,
      listSequence = REFERENCE_DATA_LIST_SEQUENCE,
      domain = ReferenceDataDomain(
        code = REFERENCE_DATA_DOMAIN_CODE,
        createdBy = USER1,
        createdAt = NOW,
        listSequence = REFERENCE_DATA_LIST_SEQUENCE,
        description = REFERENCE_DATA_DOMAIN_DESCRIPTION,
      ),
    )

    val PRISONER_SEARCH_RESPONSE = PrisonerDto(
      PRISONER_NUMBER,
      123,
      "prisoner",
      "middle",
      "lastName",
      LocalDate.of(1988, 3, 4),
    )
  }
}
