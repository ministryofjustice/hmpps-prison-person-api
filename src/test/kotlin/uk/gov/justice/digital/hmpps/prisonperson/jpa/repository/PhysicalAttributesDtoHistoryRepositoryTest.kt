package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributesHistory
import java.time.ZonedDateTime

class PhysicalAttributesDtoHistoryRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @Autowired
  lateinit var physicalAttributesHistoryRepository: PhysicalAttributesHistoryRepository

  @Test
  fun `can persist physical attributes history`() {
    val time = ZonedDateTime.now(clock)

    val physicalAttributes = PhysicalAttributes(prisonerNumber = "A1234AA")
    physicalAttributesRepository.save(PhysicalAttributes(prisonerNumber = "A1234AA"))

    val physicalAttributesHistory = PhysicalAttributesHistory(
      physicalAttributes = physicalAttributes,
      height = 180,
      weight = 70,
      migratedAt = time,
      createdAt = time,
      createdBy = "USER1",
      appliesFrom = time.minusDays(2),
      appliesTo = time.minusDays(1),
    )

    val id = physicalAttributesHistoryRepository.save(physicalAttributesHistory).physicalAttributesHistoryId

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(physicalAttributesHistoryRepository.getReferenceById(id)) {
      assertThat(height).isEqualTo(180)
      assertThat(weight).isEqualTo(70)
      assertThat(migratedAt).isEqualTo(time)
      assertThat(createdAt).isEqualTo(time)
      assertThat(createdBy).isEqualTo("USER1")
      assertThat(appliesFrom).isEqualTo(time.minusDays(2))
      assertThat(appliesTo).isEqualTo(time.minusDays(1))
    }
  }
}
