package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.Clock
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PhysicalAttributesServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var authenticationFacade: AuthenticationFacade

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: PhysicalAttributesService

  private val savedPhysicalAttributes = argumentCaptor<PhysicalAttributes>()

  @Nested
  inner class GetPhysicalAttributes {
    @Test
    fun `physical attributes not found`() {
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(underTest.getPhysicalAttributes(PRISONER_NUMBER)).isNull()
    }

    @Test
    fun `physical attributes retrieved`() {
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            height = PRISONER_HEIGHT,
            weight = PRISONER_WEIGHT,
            fieldMetadata = mutableMapOf(
              HEIGHT to FieldMetadata(PRISONER_NUMBER, HEIGHT, THEN, USER1),
              WEIGHT to FieldMetadata(PRISONER_NUMBER, WEIGHT, THEN, USER1),
            ),
          ),
        ),
      )

      assertThat(underTest.getPhysicalAttributes(PRISONER_NUMBER))
        .isEqualTo(
          PhysicalAttributesDto(
            height = ValueWithMetadata(PRISONER_HEIGHT, THEN, USER1),
            weight = ValueWithMetadata(PRISONER_WEIGHT, THEN, USER1),
          ),
        )
    }
  }

  @Nested
  inner class CreateOrUpdatePhysicalAttributes {

    @BeforeEach
    fun beforeEach() {
      whenever(physicalAttributesRepository.save(savedPhysicalAttributes.capture()))
        .thenAnswer { savedPhysicalAttributes.firstValue }

      whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
    }

    @Test
    fun `creates new physical attributes`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(PRISONER_SEARCH_RESPONSE)
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_UPDATE_REQUEST))
        .isEqualTo(
          PhysicalAttributesDto(
            height = ValueWithMetadata(PRISONER_HEIGHT, NOW, USER1),
            weight = ValueWithMetadata(PRISONER_WEIGHT, NOW, USER1),
            hair = ValueWithMetadata(null, NOW, USER1),
            facialHair = ValueWithMetadata(null, NOW, USER1),
            face = ValueWithMetadata(null, NOW, USER1),
            build = ValueWithMetadata(null, NOW, USER1),
            leftEyeColour = ValueWithMetadata(null, NOW, USER1),
            rightEyeColour = ValueWithMetadata(null, NOW, USER1),
            shoeSize = ValueWithMetadata(null, NOW, USER1),
          ),
        )

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = PRISONER_HEIGHT,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = PRISONER_WEIGHT,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )
      }
    }

    @Test
    fun `does not create new physical attributes if prisoner doesn't exist in prisoner search`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy { underTest.createOrUpdate(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_UPDATE_REQUEST) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("Prisoner number '${PRISONER_NUMBER}' not found")

      verify(physicalAttributesRepository, never()).save(any())
    }

    @Test
    fun `does not create new physical attributes if prisoner found in prisoner search matched on a different id`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(PrisonerDto(prisonerNumber = "somethingelse"))
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy { underTest.createOrUpdate(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_UPDATE_REQUEST) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("Prisoner number '${PRISONER_NUMBER}' not found")

      verify(physicalAttributesRepository, never()).save(any())
    }

    @Test
    fun `updates physical attributes`() {
      whenever(physicalAttributesRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PhysicalAttributes(
            prisonerNumber = PRISONER_NUMBER,
            height = PREVIOUS_PRISONER_HEIGHT,
            weight = PREVIOUS_PRISONER_WEIGHT,
          ).also { it.updateFieldHistory(lastModifiedAt = NOW.minusDays(1), lastModifiedBy = USER2) },
        ),
      )

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_UPDATE_REQUEST))
        .isEqualTo(
          PhysicalAttributesDto(
            height = ValueWithMetadata(PRISONER_HEIGHT, NOW, USER1),
            weight = ValueWithMetadata(PRISONER_WEIGHT, NOW, USER1),
            hair = ValueWithMetadata(null, NOW.minusDays(1), USER2),
            facialHair = ValueWithMetadata(null, NOW.minusDays(1), USER2),
            face = ValueWithMetadata(null, NOW.minusDays(1), USER2),
            build = ValueWithMetadata(null, NOW.minusDays(1), USER2),
            leftEyeColour = ValueWithMetadata(null, NOW.minusDays(1), USER2),
            rightEyeColour = ValueWithMetadata(null, NOW.minusDays(1), USER2),
            shoeSize = ValueWithMetadata(null, NOW.minusDays(1), USER2),
          ),
        )

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)

        expectFieldHistory(
          HEIGHT,
          // Initial history entry:
          HistoryComparison(
            value = PREVIOUS_PRISONER_HEIGHT,
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = NOW.minusDays(1),
            appliesTo = NOW,
          ),
          // New history entry:
          HistoryComparison(
            value = PRISONER_HEIGHT,
            createdAt = NOW,
            createdBy = USER1,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          // Initial history entry:
          HistoryComparison(
            value = PREVIOUS_PRISONER_WEIGHT,
            createdAt = NOW.minusDays(1),
            createdBy = USER2,
            appliesFrom = NOW.minusDays(1),
            appliesTo = NOW,
          ),
          // New history entry:
          HistoryComparison(
            value = PRISONER_WEIGHT,
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
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70
    const val PREVIOUS_PRISONER_HEIGHT = 179
    const val PREVIOUS_PRISONER_WEIGHT = 69
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW: ZonedDateTime = ZonedDateTime.now()
    val THEN: ZonedDateTime = NOW.minusDays(1)

    val attributes = mutableMapOf<String, Any?>(
      Pair("height", PRISONER_HEIGHT),
      Pair("weight", PRISONER_WEIGHT),
    )

    val PHYSICAL_ATTRIBUTES_UPDATE_REQUEST = PhysicalAttributesUpdateRequest(attributes)
    val PRISONER_SEARCH_RESPONSE = PrisonerDto(PRISONER_NUMBER)
  }
}
