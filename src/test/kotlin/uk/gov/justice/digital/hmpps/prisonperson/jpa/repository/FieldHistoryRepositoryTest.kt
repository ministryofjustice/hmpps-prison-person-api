package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class FieldHistoryRepositoryTest : RepositoryTest() {

  @Test
  fun `can persist field history - valueInt`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = WEIGHT,
      valueInt = INT_VALUE,
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
      assertThat(valueInt).isEqualTo(INT_VALUE)
      assertThat(valueString).isNull()
      assertThat(valueRef).isNull()
      assertThat(migratedAt).isEqualTo(NOW)
      assertThat(createdAt).isEqualTo(NOW)
      assertThat(createdBy).isEqualTo(USER1)
      assertThat(appliesFrom).isEqualTo(NOW.minusDays(2))
      assertThat(appliesTo).isEqualTo(NOW.minusDays(1))
      assertThat(source).isEqualTo(DPS)
    }
  }

  @Test
  fun `can persist field history - valueString`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = WEIGHT,
      valueString = STRING_VALUE,
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
      assertThat(valueInt).isNull()
      assertThat(valueString).isEqualTo(STRING_VALUE)
      assertThat(valueRef).isNull()
      assertThat(migratedAt).isEqualTo(NOW)
      assertThat(createdAt).isEqualTo(NOW)
      assertThat(createdBy).isEqualTo(USER1)
      assertThat(appliesFrom).isEqualTo(NOW.minusDays(2))
      assertThat(appliesTo).isEqualTo(NOW.minusDays(1))
      assertThat(source).isEqualTo(DPS)
    }
  }

  @Test
  fun `can persist field history - valueRef`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = WEIGHT,
      valueRef = REF_DATA_CODE,
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
      assertThat(valueInt).isNull()
      assertThat(valueString).isNull()
      assertThat(valueRef).isEqualTo(REF_DATA_CODE)
      assertThat(migratedAt).isEqualTo(NOW)
      assertThat(createdAt).isEqualTo(NOW)
      assertThat(createdBy).isEqualTo(USER1)
      assertThat(appliesFrom).isEqualTo(NOW.minusDays(2))
      assertThat(appliesTo).isEqualTo(NOW.minusDays(1))
      assertThat(source).isEqualTo(DPS)
    }
  }

  @Test
  fun `fails to persist field history if multiple valueXXX properties are set`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = WEIGHT,
      valueInt = INT_VALUE,
      valueRef = REF_DATA_CODE,
      migratedAt = NOW,
      createdAt = NOW,
      createdBy = USER1,
      appliesFrom = NOW.minusDays(2),
      appliesTo = NOW.minusDays(1),
      source = DPS,
    )

    assertThrows(DataIntegrityViolationException::class.java) {
      fieldHistoryRepository.save(fieldHistory).fieldHistoryId

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()
    }
  }

  @Test
  fun `fails to persist field history if value_ref is not a valid value`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = WEIGHT,
      valueRef = INVALID_REF_DATA_CODE,
      migratedAt = NOW,
      createdAt = NOW,
      createdBy = USER1,
      appliesFrom = NOW.minusDays(2),
      appliesTo = NOW.minusDays(1),
      source = DPS,
    )

    assertThrows(JpaObjectRetrievalFailureException::class.java) {
      fieldHistoryRepository.save(fieldHistory).fieldHistoryId

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()
    }
  }

  @Test
  fun `can check for equality`() {
    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = WEIGHT,
        valueInt = INT_VALUE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    ).isEqualTo(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = WEIGHT,
        valueInt = INT_VALUE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    )

    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = HAIR,
        valueRef = REF_DATA_CODE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    ).isEqualTo(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = HAIR,
        valueRef = REF_DATA_CODE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    )

    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = WEIGHT,
        valueInt = INT_VALUE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    ).isNotEqualTo(
      FieldHistory(
        prisonerNumber = "Z1234ZZ",
        field = WEIGHT,
        valueInt = INT_VALUE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    )

    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = HAIR,
        valueRef = REF_DATA_CODE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    ).isNotEqualTo(
      FieldHistory(
        prisonerNumber = "Z1234ZZ",
        field = HAIR,
        valueRef = REF_DATA_CODE,
        appliesFrom = NOW,
        createdAt = NOW,
        createdBy = USER1,
      ),
    )
  }

  @Test
  fun `toString does not cause a stack overflow`() {
    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = WEIGHT,
        valueInt = INT_VALUE,
        createdAt = NOW,
        createdBy = USER1,
      ).toString(),
    ).isInstanceOf(String::class.java)

    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = HAIR,
        valueRef = REF_DATA_CODE,
        createdAt = NOW,
        createdBy = USER1,
      ).toString(),
    ).isInstanceOf(String::class.java)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    const val INT_VALUE = 123
    const val STRING_VALUE = "VALUE"
    val REF_DATA_DOMAIN = ReferenceDataDomain("TEST", "Test domain", 1, ZonedDateTime.now(), "testUser")
    val REF_DATA_CODE = ReferenceDataCode(
      id = "TEST_ORANGE",
      domain = REF_DATA_DOMAIN,
      code = "ORANGE",
      description = "Orange",
      listSequence = 1,
      createdAt = ZonedDateTime.now(),
      createdBy = "testUser",
    )
    val INVALID_REF_DATA_CODE = ReferenceDataCode(
      id = "TEST_INVALID",
      domain = REF_DATA_DOMAIN,
      code = "INVALID",
      description = "INVALID",
      listSequence = 1,
      createdAt = ZonedDateTime.now(),
      createdBy = "testUser",
    )

    val NOW: ZonedDateTime = ZonedDateTime.now(clock)
  }
}
