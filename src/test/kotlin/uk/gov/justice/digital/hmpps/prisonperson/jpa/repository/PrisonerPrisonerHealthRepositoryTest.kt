package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class PrisonerPrisonerHealthRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: PrisonerHealthRepository

  fun save(prisonerHealth: PrisonerHealth) {
    repository.save(prisonerHealth)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `can persist health`() {
    val prisonerHealth = PrisonerHealth(PRISONER_NUMBER, SMOKER_OR_VAPER)
    save(prisonerHealth)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isEqualTo(SMOKER_OR_VAPER)
    }
  }

  @Test
  fun `can persist health with null values`() {
    val prisonerHealth = PrisonerHealth(PRISONER_NUMBER)
    save(prisonerHealth)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isNull()
    }
  }

  @Test
  fun `can update health`() {
    repository.save(PrisonerHealth(PRISONER_NUMBER))
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
    assertThat(PrisonerHealth(PRISONER_NUMBER)).isEqualTo(PrisonerHealth(PRISONER_NUMBER))

    assertThat(PrisonerHealth(PRISONER_NUMBER)).isNotEqualTo(PrisonerHealth("Example"))
  }

  @Test
  fun `toString does not cause stack overflow`() {
    // TODO: When theres history
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

    val REF_DATA_DOMAIN = ReferenceDataDomain("SMOKE", "Smoker or vaper", 0, ZonedDateTime.now(), "OMS_OWNER")
    val REF_DATA_CODE = ReferenceDataCode(
      id = "SMOKE_SMOKER",
      domain = REF_DATA_DOMAIN,
      code = "SMOKER",
      description = "Yes, they smoke",
      listSequence = 0,
      createdAt = ZonedDateTime.now(),
      createdBy = "OMS_OWNER",
    )

    val SMOKER_OR_VAPER = REF_DATA_CODE
  }
}
