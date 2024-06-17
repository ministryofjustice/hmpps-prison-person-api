package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.AbstractAuditable_.createdBy
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import java.time.ZonedDateTime

class PhysicalAttributesDtoRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: PhysicalAttributesRepository

  @Test
  fun `can persist physical attributes history`() {
    val physicalAttributes = PhysicalAttributes(
      PRISONER_NUMBER,
      PRISONER_HEIGHT,
      PRISONER_WEIGHT,
      migratedAt = NOW,
      createdAt = NOW,
      createdBy = USER1,
      lastModifiedAt = NOW.plusDays(1),
      lastModifiedBy = USER2,
    )

    repository.save(physicalAttributes)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(height).isEqualTo(PRISONER_HEIGHT)
      assertThat(weight).isEqualTo(PRISONER_WEIGHT)
      assertThat(migratedAt).isEqualTo(NOW)
      assertThat(createdAt).isEqualTo(NOW)
      assertThat(createdBy).isEqualTo(USER1)
      assertThat(lastModifiedAt).isEqualTo(NOW.plusDays(1))
      assertThat(lastModifiedBy).isEqualTo(USER2)
    }
  }

  @Test
  fun `can persist physical attributes with null fields`() {
    repository.save(PhysicalAttributes(PRISONER_NUMBER, createdBy = USER1, lastModifiedBy = USER2))

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
    repository.save(PhysicalAttributes(PRISONER_NUMBER, createdBy = USER1, lastModifiedAt = NOW, lastModifiedBy = USER2))
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val physicalAttributes = repository.getReferenceById("A1234AA")
    physicalAttributes.height = 180
    physicalAttributes.weight = 70
    physicalAttributes.lastModifiedAt = NOW.plusDays(1)

    repository.save(physicalAttributes)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(height).isEqualTo(PRISONER_HEIGHT)
      assertThat(weight).isEqualTo(PRISONER_WEIGHT)
      assertThat(lastModifiedAt).isEqualTo(NOW.plusDays(1))
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_HEIGHT = 180
    const val PRISONER_WEIGHT = 70
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW: ZonedDateTime = ZonedDateTime.now(clock)
  }
}
