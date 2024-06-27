package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributesHistory
import java.time.ZonedDateTime

class PhysicalAttributesHistoryRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Autowired
  lateinit var physicalAttributesHistoryRepository: PhysicalAttributesHistoryRepository

  @Test
  fun `can persist physical attributes history`() {
    val physicalAttributes = PhysicalAttributes(PRISONER_NUMBER, createdBy = USER1, lastModifiedBy = USER1)
      .also { physicalAttributesRepository.save(it) }

    val physicalAttributesHistory = PhysicalAttributesHistory(
      physicalAttributes = physicalAttributes,
      height = 180,
      weight = 70,
      migratedAt = NOW,
      createdAt = NOW,
      createdBy = USER1,
      appliesFrom = NOW.minusDays(2),
      appliesTo = NOW.minusDays(1),
    )

    val id = physicalAttributesHistoryRepository.save(physicalAttributesHistory).physicalAttributesHistoryId

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(physicalAttributesHistoryRepository.getReferenceById(id)) {
      assertThat(height).isEqualTo(PRISONER_HEIGHT)
      assertThat(weight).isEqualTo(PRISONER_WEIGHT)
      assertThat(migratedAt).isEqualTo(NOW)
      assertThat(createdAt).isEqualTo(NOW)
      assertThat(createdBy).isEqualTo(USER1)
      assertThat(appliesFrom).isEqualTo(NOW.minusDays(2))
      assertThat(appliesTo).isEqualTo(NOW.minusDays(1))
    }
  }

  @Test
  fun `can check for equality`() {
    assertThat(
      PhysicalAttributes(PRISONER_NUMBER, createdBy = USER1, lastModifiedAt = NOW, lastModifiedBy = USER1)
        .also { it.addToHistory() }
        .history.first(),
    ).isEqualTo(
      PhysicalAttributes(PRISONER_NUMBER, createdBy = USER1, lastModifiedAt = NOW, lastModifiedBy = USER1)
        .also { it.addToHistory() }
        .history.first(),
    )

    assertThat(
      PhysicalAttributes(PRISONER_NUMBER, createdBy = USER1, lastModifiedAt = NOW, lastModifiedBy = USER1)
        .also { it.addToHistory() }
        .history.first(),
    ).isNotEqualTo(
      PhysicalAttributes("Z1234ZZ", createdBy = USER1, lastModifiedAt = NOW, lastModifiedBy = USER1)
        .also { it.addToHistory() }
        .history.first(),
    )
  }

  @Test
  fun `toString does not cause stack overflow`() {
    assertThat(
      PhysicalAttributes(PRISONER_NUMBER, createdBy = USER1, lastModifiedBy = USER1)
        .also { it.addToHistory() }
        .history.first()
        .toString(),
    ).isInstanceOf(String::class.java)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70
    const val USER1 = "USER1"

    val NOW: ZonedDateTime = ZonedDateTime.now(clock)
  }
}
