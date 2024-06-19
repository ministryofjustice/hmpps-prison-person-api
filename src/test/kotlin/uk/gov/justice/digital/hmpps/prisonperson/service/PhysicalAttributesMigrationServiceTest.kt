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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class PhysicalAttributesMigrationServiceTest {
  @Mock
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

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
    }

    @Test
    fun `migrates a single version of physical attributes`() {
      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(PHYSICAL_ATTRIBUTES_SYNC_REQUEST)))
        .isEqualTo(PhysicalAttributesDto(PRISONER_HEIGHT, PRISONER_WEIGHT))

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)
        assertThat(createdAt).isEqualTo(THEN)
        assertThat(createdBy).isEqualTo(USER1)
        assertThat(lastModifiedAt).isEqualTo(THEN)
        assertThat(lastModifiedBy).isEqualTo(USER1)
        assertThat(migratedAt).isEqualTo(NOW)

        assertThat(history).hasSize(1)
        with(history.first()) {
          assertThat(height).isEqualTo(PRISONER_HEIGHT)
          assertThat(weight).isEqualTo(PRISONER_WEIGHT)
          assertThat(createdAt).isEqualTo(THEN)
          assertThat(createdBy).isEqualTo(USER1)
          assertThat(appliesFrom).isEqualTo(THEN)
          assertThat(appliesTo).isNull()
          assertThat(migratedAt).isEqualTo(NOW)
        }
      }
    }

    @Test
    fun `migrates a history of physical attributes`() {
      val record1 = PHYSICAL_ATTRIBUTES_SYNC_REQUEST
      val record2 = generatePrevious(record1, USER2)
      val record3 = generatePrevious(record2, USER3)

      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(record1, record2, record3)))
        .isEqualTo(PhysicalAttributesDto(PRISONER_HEIGHT, PRISONER_WEIGHT))

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(PRISONER_HEIGHT)
        assertThat(weight).isEqualTo(PRISONER_WEIGHT)
        assertThat(createdAt).isEqualTo(THEN.minusDays(2))
        assertThat(createdBy).isEqualTo(USER3)
        assertThat(lastModifiedAt).isEqualTo(THEN)
        assertThat(lastModifiedBy).isEqualTo(USER1)
        assertThat(migratedAt).isEqualTo(NOW)

        assertThat(history).hasSize(3)
        with(getHistoryAsList()[2]) {
          assertThat(height).isEqualTo(PRISONER_HEIGHT)
          assertThat(weight).isEqualTo(PRISONER_WEIGHT)
          assertThat(createdAt).isEqualTo(THEN)
          assertThat(createdBy).isEqualTo(USER1)
          assertThat(appliesFrom).isEqualTo(THEN)
          assertThat(appliesTo).isNull()
          assertThat(migratedAt).isEqualTo(NOW)
        }
        with(getHistoryAsList()[1]) {
          assertThat(height).isEqualTo(PRISONER_HEIGHT - 1)
          assertThat(weight).isEqualTo(PRISONER_WEIGHT - 1)
          assertThat(createdAt).isEqualTo(THEN.minusDays(1))
          assertThat(createdBy).isEqualTo(USER2)
          assertThat(appliesFrom).isEqualTo(THEN.minusDays(1))
          assertThat(appliesTo).isEqualTo(THEN)
          assertThat(migratedAt).isEqualTo(NOW)
        }
        with(getHistoryAsList()[0]) {
          assertThat(height).isEqualTo(PRISONER_HEIGHT - 2)
          assertThat(weight).isEqualTo(PRISONER_WEIGHT - 2)
          assertThat(createdAt).isEqualTo(THEN.minusDays(2))
          assertThat(createdBy).isEqualTo(USER3)
          assertThat(appliesFrom).isEqualTo(THEN.minusDays(2))
          assertThat(appliesTo).isEqualTo(THEN.minusDays(1))
          assertThat(migratedAt).isEqualTo(NOW)
        }
      }
    }

    @Test
    fun `migrates physical attributes with null height and weight`() {
      assertThat(underTest.migrate(PRISONER_NUMBER, sortedSetOf(PHYSICAL_ATTRIBUTES_SYNC_REQUEST.copy(height = null, weight = null))))
        .isEqualTo(PhysicalAttributesDto(null, null))

      with(savedPhysicalAttributes.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(height).isEqualTo(null)
        assertThat(weight).isEqualTo(null)
        assertThat(createdAt).isEqualTo(THEN)
        assertThat(createdBy).isEqualTo(USER1)
        assertThat(lastModifiedAt).isEqualTo(THEN)
        assertThat(lastModifiedBy).isEqualTo(USER1)
        assertThat(migratedAt).isEqualTo(NOW)

        assertThat(history).hasSize(1)
        with(history.first()) {
          assertThat(height).isEqualTo(null)
          assertThat(weight).isEqualTo(null)
          assertThat(createdAt).isEqualTo(THEN)
          assertThat(createdBy).isEqualTo(USER1)
          assertThat(appliesFrom).isEqualTo(THEN)
          assertThat(appliesTo).isNull()
          assertThat(migratedAt).isEqualTo(NOW)
        }
      }
    }
  }

  private fun generatePrevious(migration: PhysicalAttributesMigrationRequest, username: String): PhysicalAttributesMigrationRequest = migration.copy(
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

    val PHYSICAL_ATTRIBUTES_SYNC_REQUEST = PhysicalAttributesMigrationRequest(PRISONER_HEIGHT, PRISONER_WEIGHT, appliesFrom = THEN, createdAt = THEN, createdBy = USER1)
  }
}
