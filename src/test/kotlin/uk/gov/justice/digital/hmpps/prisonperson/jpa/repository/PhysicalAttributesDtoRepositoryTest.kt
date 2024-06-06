package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import java.time.ZonedDateTime

class PhysicalAttributesDtoRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: PhysicalAttributesRepository

  @Test
  fun `can persist physical attributes history`() {
    val time = ZonedDateTime.now(clock)
    val physicalAttributes = PhysicalAttributes(
      prisonerNumber = "A1234AA",
      height = 180,
      weight = 70,
      migratedAt = time,
      createdAt = time,
      createdBy = "USER1",
      lastModifiedAt = time.plusDays(1),
      lastModifiedBy = "USER2",
    )

    repository.save(physicalAttributes)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById("A1234AA")) {
      assertThat(prisonerNumber).isEqualTo("A1234AA")
      assertThat(height).isEqualTo(180)
      assertThat(weight).isEqualTo(70)
      assertThat(migratedAt).isEqualTo(time)
      assertThat(createdAt).isEqualTo(time)
      assertThat(createdBy).isEqualTo("USER1")
      assertThat(lastModifiedAt).isEqualTo(time.plusDays(1))
      assertThat(lastModifiedBy).isEqualTo("USER2")
    }
  }

  @Test
  fun `can persist physical attributes with null fields`() {
    repository.save(PhysicalAttributes(prisonerNumber = "A1234AA"))

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById("A1234AA")) {
      assertThat(prisonerNumber).isEqualTo("A1234AA")
      assertThat(height).isNull()
      assertThat(weight).isNull()
    }
  }

  @Test
  fun `can update physical attributes`() {
    val time = ZonedDateTime.now(clock)

    repository.save(PhysicalAttributes(prisonerNumber = "A1234AA", lastModifiedAt = time))
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val physicalAttributes = repository.getReferenceById("A1234AA")
    physicalAttributes.height = 180
    physicalAttributes.weight = 70
    physicalAttributes.lastModifiedAt = time.plusDays(1)

    repository.save(physicalAttributes)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById("A1234AA")) {
      assertThat(prisonerNumber).isEqualTo("A1234AA")
      assertThat(height).isEqualTo(180)
      assertThat(weight).isEqualTo(70)
      assertThat(lastModifiedAt).isEqualTo(time.plusDays(1))
    }
  }
}
