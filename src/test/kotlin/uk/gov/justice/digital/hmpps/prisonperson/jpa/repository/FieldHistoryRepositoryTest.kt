package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import java.time.ZonedDateTime

class FieldHistoryRepositoryTest : RepositoryTest() {

  @Test
  fun `can persist field history`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = WEIGHT,
      valueInt = 123,
      migratedAt = NOW,
      createdAt = NOW,
      createdBy = USER1,
      appliesFrom = NOW.minusDays(2),
      appliesTo = NOW.minusDays(1),
      source = DPS,
    )

    val id = fieldHistoryRepository.save(fieldHistory).fieldHistoryId

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(fieldHistoryRepository.getReferenceById(id)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(field).isEqualTo(WEIGHT)
      assertThat(migratedAt).isEqualTo(NOW)
      assertThat(createdAt).isEqualTo(NOW)
      assertThat(createdBy).isEqualTo(USER1)
      assertThat(appliesFrom).isEqualTo(NOW.minusDays(2))
      assertThat(appliesTo).isEqualTo(NOW.minusDays(1))
      assertThat(source).isEqualTo(DPS)
    }
  }

  @Test
  fun `can check for equality`() {
    assertThat(
      FieldHistory(prisonerNumber = PRISONER_NUMBER, field = WEIGHT, valueInt = 123, appliesFrom = NOW, createdAt = NOW, createdBy = USER1),
    ).isEqualTo(
      FieldHistory(prisonerNumber = PRISONER_NUMBER, field = WEIGHT, valueInt = 123, appliesFrom = NOW, createdAt = NOW, createdBy = USER1),
    )

    assertThat(
      FieldHistory(prisonerNumber = PRISONER_NUMBER, field = WEIGHT, valueInt = 123, appliesFrom = NOW, createdAt = NOW, createdBy = USER1),
    ).isNotEqualTo(
      FieldHistory(prisonerNumber = "Z1234ZZ", field = WEIGHT, valueInt = 123, appliesFrom = NOW, createdAt = NOW, createdBy = USER1),
    )
  }

  @Test
  fun `toString does not cause a stack overflow`() {
    assertThat(
      FieldHistory(prisonerNumber = PRISONER_NUMBER, field = WEIGHT, valueInt = 123, createdAt = NOW, createdBy = USER1).toString(),
    ).isInstanceOf(String::class.java)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    val NOW: ZonedDateTime = ZonedDateTime.now(clock)
  }
}
