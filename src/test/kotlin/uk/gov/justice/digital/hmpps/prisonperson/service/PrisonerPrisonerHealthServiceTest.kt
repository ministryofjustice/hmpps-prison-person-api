package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PrisonerHealthUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.jpa.*
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.Clock
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PrisonerPrisonerHealthServiceTest {
  @Mock
  lateinit var prisonerHealthRepository: PrisonerHealthRepository

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var authenticationFacade: AuthenticationFacade

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: PrisonerHealthService

  private val savedPrisonerHealth = argumentCaptor<PrisonerHealth>()

  @BeforeEach
  fun beforeEach() {
    whenever(referenceDataCodeRepository.findById(REFERENCE_DATA_CODE_ID)).thenReturn(Optional.of(SMOKER_OR_VAPER))
    whenever(referenceDataCodeRepository.findById(FOOD_REFERENCE_DATA_CODE_ID)).thenReturn(Optional.of(EGG_ALLERGY))
    whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
  }

  @Test
  fun `prison person data not found`() {
    whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

    val result = underTest.getHealth(PRISONER_NUMBER)
    assertThat(result).isNull()
  }

  @Test
  fun `prison health data is found`() {
    whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(
      Optional.of(
        PrisonerHealth(
          prisonerNumber = PRISONER_NUMBER,
          smokerOrVaper = SMOKER_OR_VAPER,
          fieldMetadata = mutableMapOf(
            PrisonPersonField.SMOKER_OR_VAPER to FieldMetadata(
              PRISONER_NUMBER,
              PrisonPersonField.SMOKER_OR_VAPER,
              NOW,
              USER1,
            ),
          ),
        ),
      ),
    )

    val result = underTest.getHealth(PRISONER_NUMBER)

    assertThat(result).isEqualTo(
      HealthDto(
        smokerOrVaper = ValueWithMetadata(
          ReferenceDataSimpleDto(
            id = REFERENCE_DATA_CODE_ID,
            description = REFERENCE_DATA_CODE_DESCRPTION,
            listSequence = REFERENCE_DATA_LIST_SEQUENCE,
            isActive = true,
          ),
          NOW,
          USER1,
        ),
      ),
    )
  }

  @Nested
  inner class CreateOrUpdatePrisonerHealth {

    @BeforeEach
    fun beforeEach() {
      whenever(prisonerHealthRepository.save(savedPrisonerHealth.capture())).thenAnswer { savedPrisonerHealth.firstValue }
    }

    @Test
    fun `creating new health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PRISONER_SEARCH_RESPONSE,
      )

      whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(
        underTest.createOrUpdate(
          PRISONER_NUMBER,
          HEALTH_UPDATE_REQUEST,
        ),
      ).isEqualTo(
        HealthDto(
          smokerOrVaper = ValueWithMetadata(SMOKER_OR_VAPER.toSimpleDto(), NOW, USER1),
          foodAllergies = listOf(EGG_ALLERGY.toSimpleDto()),
        ),
      )

      with(savedPrisonerHealth.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(smokerOrVaper).isEqualTo(SMOKER_OR_VAPER)
        assertThat(foodAllergies).containsAll(listOf(EGG_FOOD_ALLERGY))

        expectFieldHistory(
          PrisonPersonField.SMOKER_OR_VAPER,
          fieldHistory,
          HistoryComparison(
            value = SMOKER_OR_VAPER,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )
      }
    }

    @Test
    fun `updating health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PRISONER_SEARCH_RESPONSE,
      )

      whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PrisonerHealth(
            prisonerNumber = PRISONER_NUMBER,
            smokerOrVaper = SMOKER_OR_VAPER,
            foodAllergies = mutableSetOf(EGG_FOOD_ALLERGY),
          ).also { it.updateFieldHistory(lastModifiedAt = NOW.minusDays(1), lastModifiedBy = USER2) },
        ),
      )

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, HEALTH_UPDATE_REQUEST_WITH_NULL)).isEqualTo(
        HealthDto(
          ValueWithMetadata(null, NOW, USER1),
          null
        ),
      )

      with(savedPrisonerHealth.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(smokerOrVaper).isEqualTo(null)
        assertThat(foodAllergies).isEqualTo(null)

        expectFieldHistory(
          PrisonPersonField.SMOKER_OR_VAPER,
          fieldHistory,
          HistoryComparison(
            value = SMOKER_OR_VAPER,
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = NOW.minusDays(1),
            appliesTo = NOW,
          ),
          HistoryComparison(
            value = null,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )
      }
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW = ZonedDateTime.now()

    val REFERENCE_DATA_CODE_ID = "EXAMPLE_CODE"
    val REFERENCE_DATA_CODE = "CODE"
    val REFERENCE_DATA_CODE_DESCRPTION = "Example code"
    val REFERENCE_DATA_LIST_SEQUENCE = 0
    val REFERENCE_DATA_DOMAIN_CODE = "EXAMPLE"
    val REFERENCE_DATA_DOMAIN_DESCRIPTION = "Example"

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

    val FOOD_REFERENCE_DATA_CODE_ID = "FOOD_EXAMPLE_CODE"
    val FOOD_REFERENCE_DATA_CODE = "FOOD_CODE"
    val FOOD_REFERENCE_DATA_CODE_DESCRPTION = "Example food code"
    val FOOD_REFERENCE_DATA_LIST_SEQUENCE = 0
    val FOOD_REFERENCE_DATA_DOMAIN_CODE = "FOOD_EXAMPLE"
    val FOOD_REFERENCE_DATA_DOMAIN_DESCRIPTION = "Food Example"

    val EGG_ALLERGY = ReferenceDataCode(
      id = FOOD_REFERENCE_DATA_CODE_ID,
      code = FOOD_REFERENCE_DATA_CODE,
      createdBy = USER1,
      createdAt = NOW,
      description = FOOD_REFERENCE_DATA_CODE_DESCRPTION,
      listSequence = FOOD_REFERENCE_DATA_LIST_SEQUENCE,
      domain = ReferenceDataDomain(
        code = FOOD_REFERENCE_DATA_DOMAIN_CODE,
        createdBy = USER1,
        createdAt = NOW,
        listSequence = FOOD_REFERENCE_DATA_LIST_SEQUENCE,
        description = FOOD_REFERENCE_DATA_DOMAIN_DESCRIPTION,
      ),
    )

    val EGG_FOOD_ALLERGY = FoodAllergy(FoodAllergyId(PRISONER_NUMBER, EGG_ALLERGY))

    val PRISONER_SEARCH_RESPONSE = PrisonerDto(PRISONER_NUMBER)

    val attributes = mutableMapOf<String, Any?>(
      Pair("smokerOrVaper", REFERENCE_DATA_CODE_ID),
      Pair("foodAllergies", listOf(FOOD_REFERENCE_DATA_CODE_ID)),
    )
    val HEALTH_UPDATE_REQUEST = PrisonerHealthUpdateRequest(attributes)

    val attributes_undefined = mutableMapOf<String, Any?>(
      Pair("smokerOrVaper", null),
      Pair("foodAllergies", null),
    )
    val HEALTH_UPDATE_REQUEST_WITH_NULL = PrisonerHealthUpdateRequest(attributes_undefined)

  }
}
