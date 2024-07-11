package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.AbstractAuditable_.lastModifiedBy
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import java.time.ZonedDateTime

class PhysicalAttributesRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: PhysicalAttributesRepository

  @Test
  fun `can persist physical attributes history`() {
    val physicalAttributes = PhysicalAttributes(
      PRISONER_NUMBER,
      PRISONER_HEIGHT,
      PRISONER_WEIGHT,
    )

    repository.save(physicalAttributes)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(height).isEqualTo(PRISONER_HEIGHT)
      assertThat(weight).isEqualTo(PRISONER_WEIGHT)
    }
  }

  @Test
  fun `can persist physical attributes with null fields`() {
    repository.save(PhysicalAttributes(PRISONER_NUMBER))

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
    repository.save(PhysicalAttributes(PRISONER_NUMBER))
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val physicalAttributes = repository.getReferenceById("A1234AA")
    physicalAttributes.height = 180
    physicalAttributes.weight = 70

    repository.save(physicalAttributes)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(height).isEqualTo(PRISONER_HEIGHT)
      assertThat(weight).isEqualTo(PRISONER_WEIGHT)
    }
  }

  @Test
  fun `can check for equality`() {
    assertThat(
      PhysicalAttributes(PRISONER_NUMBER),
    ).isEqualTo(
      PhysicalAttributes(PRISONER_NUMBER),
    )

    assertThat(
      PhysicalAttributes(PRISONER_NUMBER),
    ).isNotEqualTo(
      PhysicalAttributes("Z1234ZZ"),
    )
  }

  @Test
  fun `toString does not cause stack overflow`() {
    assertThat(
      PhysicalAttributes(PRISONER_NUMBER)
        .also { it.updateFieldHistory(lastModifiedAt = NOW, lastModifiedBy = USER1) }
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
