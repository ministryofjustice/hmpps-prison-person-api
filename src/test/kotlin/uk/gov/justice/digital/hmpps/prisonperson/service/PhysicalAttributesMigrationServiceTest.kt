package uk.gov.justice.digital.hmpps.prisonperson.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesMigrationResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_MIGRATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectDomainEventRaised
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.PrisonPersonUpdatedEvent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class PhysicalAttributesMigrationServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Mock
  lateinit var physicalAttributesService: PhysicalAttributesService

  @Mock
  lateinit var telemetryClient: TelemetryClient

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: PhysicalAttributesMigrationService

  private val savedPhysicalAttributes = argumentCaptor<PhysicalAttributes>()

  @Nested
  inner class MigratePhysicalAttributes {

    @BeforeEach
    fun beforeEach() {
      whenever(physicalAttributesRepository.save(savedPhysicalAttributes.capture())).thenAnswer { savedPhysicalAttributes.firstValue }
      whenever(physicalAttributesService.newPhysicalAttributesFor(PRISONER_NUMBER)).thenReturn(
        PhysicalAttributes(
          PRISONER_NUMBER,
        ),
      )
    }

    @Test
    fun `migrates a single version of physical attributes`() {
      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST)))
        .isInstanceOf(PhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = PRISONER_HEIGHT,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = PRISONER_WEIGHT,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        assertThat(domainEvents()).hasSize(1)
        with(domainEvents().first() as PrisonPersonUpdatedEvent) {
          assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
          assertThat(eventType).isEqualTo(PHYSICAL_ATTRIBUTES_MIGRATED)
          assertThat(fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `migrates a history of physical attributes`() {
      val record1 = PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST
      val record2 = generatePrevious(record1, USER2)
      val record3 = generatePrevious(record2, USER3)

      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(record1, record2, record3)))
        .isInstanceOf(PhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = PRISONER_HEIGHT - 2,
            createdAt = THEN.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = PRISONER_HEIGHT - 1,
            createdAt = THEN.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = PRISONER_HEIGHT,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = PRISONER_WEIGHT - 2,
            createdAt = THEN.minusDays(2),
            createdBy = USER3,
            appliesFrom = THEN.minusDays(2),
            appliesTo = THEN.minusDays(1),
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = PRISONER_WEIGHT - 1,
            createdAt = THEN.minusDays(1),
            createdBy = USER2,
            appliesFrom = THEN.minusDays(1),
            appliesTo = THEN,
            migratedAt = NOW,
            source = NOMIS,
          ),
          HistoryComparison(
            value = PRISONER_WEIGHT,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        assertThat(domainEvents()).hasSize(1)
        with(domainEvents().first() as PrisonPersonUpdatedEvent) {
          assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
          assertThat(eventType).isEqualTo(PHYSICAL_ATTRIBUTES_MIGRATED)
          assertThat(fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `ignores history record if physical attributes have not changed`() {
      val record1 = PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST
      val record2 = record1.copy(appliesFrom = record1.appliesFrom.plusDays(1))

      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(record1, record2)))
        .isInstanceOf(PhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = PRISONER_HEIGHT,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = PRISONER_WEIGHT,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectDomainEventRaised(PRISONER_NUMBER, PHYSICAL_ATTRIBUTES_MIGRATED) {
          assertThat((it as PrisonPersonUpdatedEvent).fieldHistoryIds).isNotEmpty()
        }
      }
    }

    @Test
    fun `migrates physical attributes with null height and weight`() {
      assertThat(
        underTest.migrate(
          PRISONER_NUMBER,
          sortedSetOf(PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST.copy(height = null, weight = null)),
        ),
      )
        .isInstanceOf(PhysicalAttributesMigrationResponse::class.java)

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(null)
        assertThat(weight).isEqualTo(null)

        expectFieldHistory(
          HEIGHT,
          HistoryComparison(
            value = null,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        expectFieldHistory(
          WEIGHT,
          HistoryComparison(
            value = null,
            createdAt = THEN,
            createdBy = USER1,
            appliesFrom = THEN,
            appliesTo = null,
            migratedAt = NOW,
            source = NOMIS,
          ),
        )

        assertThat(domainEvents()).hasSize(1)
        with(domainEvents().first() as PrisonPersonUpdatedEvent) {
          assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
          assertThat(eventType).isEqualTo(PHYSICAL_ATTRIBUTES_MIGRATED)
          assertThat(fieldHistoryIds).isNotEmpty()
        }
      }
    }
  }

  @Test
  fun `handles empty migration`() {
    assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf())).isEqualTo(PhysicalAttributesMigrationResponse())

    verifyNoInteractions(physicalAttributesRepository)
  }

  private fun generatePrevious(
    migration: PhysicalAttributesMigrationRequest,
    username: String,
  ): PhysicalAttributesMigrationRequest = migration.copy(
    height = migration.height?.minus(1),
    weight = migration.weight?.minus(1),
    appliesFrom = migration.appliesFrom.minusDays(1),
    appliesTo = migration.appliesFrom,
    createdAt = migration.createdAt.minusDays(1),
    createdBy = username,
  )

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70
    const val USER1 = "USER1"
    const val USER2 = "USER2"
    const val USER3 = "USER3"

    val NOW: ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/London"))
    val THEN: ZonedDateTime = NOW.minusDays(1)

    val PHYSICAL_ATTRIBUTES_MIGRATION_REQUEST = PhysicalAttributesMigrationRequest(
      PRISONER_HEIGHT,
      PRISONER_WEIGHT,
      appliesFrom = THEN,
      createdAt = THEN,
      createdBy = USER1,
      latestBooking = true,
    )
  }
}
