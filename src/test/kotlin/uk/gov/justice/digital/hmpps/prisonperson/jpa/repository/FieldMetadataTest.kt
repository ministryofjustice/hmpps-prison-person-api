package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.AbstractAuditable_.lastModifiedBy
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldName.WEIGHT
import java.time.ZonedDateTime

class FieldMetadataTest : RepositoryTest() {

  @Autowired
  lateinit var repository: FieldMetadataRepository

  @Test
  fun `can persist field metadata`() {
    repository.save(
      FieldMetadata(
        PRISONER_NUMBER,
        WEIGHT,
        lastModifiedAt = NOW,
        lastModifiedBy = USER1,
      ),
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.findAllByPrisonerNumber(PRISONER_NUMBER)[0]) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(field).isEqualTo(WEIGHT)
      assertThat(lastModifiedAt).isEqualTo(NOW)
      assertThat(lastModifiedBy).isEqualTo(USER1)
    }
  }

  @Test
  fun `can update field metadata`() {
    repository.save(
      FieldMetadata(
        PRISONER_NUMBER,
        WEIGHT,
        lastModifiedAt = NOW,
        lastModifiedBy = USER1,
      ),
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val fieldMetadata = repository.findAllByPrisonerNumber(PRISONER_NUMBER)[0]
    fieldMetadata.lastModifiedAt = NOW.plusDays(1)
    fieldMetadata.lastModifiedBy = USER2

    repository.save(fieldMetadata)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.findAllByPrisonerNumber(PRISONER_NUMBER)[0]) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(field).isEqualTo(WEIGHT)
      assertThat(lastModifiedAt).isEqualTo(NOW.plusDays(1))
      assertThat(lastModifiedBy).isEqualTo(USER2)
    }
  }

  @Test
  fun `can check for equality`() {
    assertThat(
      FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
    ).isEqualTo(
      FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
    )

    assertThat(
      FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
    ).isNotEqualTo(
      FieldMetadata("Z1234ZZ", WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1),
    )
  }

  @Test
  fun `toString does not cause stack overflow`() {
    assertThat(
      FieldMetadata(PRISONER_NUMBER, WEIGHT, lastModifiedAt = NOW, lastModifiedBy = USER1).toString(),
    ).isInstanceOf(String::class.java)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW: ZonedDateTime = ZonedDateTime.now(clock)
  }
}
