package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.Health
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class HealthRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: HealthRepository

  fun save(health: Health) {
    repository.save(health)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `can persist health`() {
    val health = Health(PRISONER_NUMBER, SMOKER_OR_VAPER)
    save(health)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isEqualTo(SMOKER_OR_VAPER)
    }
  }

  @Test
  fun `can persist health with null values`() {
    val health = Health(PRISONER_NUMBER)
    save(health)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isNull()
    }
  }

  @Test
  fun `can update health`() {
    repository.save(Health(PRISONER_NUMBER))
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val health = repository.getReferenceById(PRISONER_NUMBER)
    health.smokerOrVaper = SMOKER_OR_VAPER

    repository.save(health)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isEqualTo(SMOKER_OR_VAPER)
    }
  }

  @Test
  fun `can test for equality`() {
    assertThat(Health(PRISONER_NUMBER)).isEqualTo(Health(PRISONER_NUMBER))

    assertThat(Health(PRISONER_NUMBER)).isNotEqualTo(Health("Example"))
  }

  @Test
  fun `toString does not cause stack overflow`() {
    // TODO: When theres history
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

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

    val SMOKER_OR_VAPER = REF_DATA_CODE
  }
}
