package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.prisonperson.jpa.MedicalDietaryRequirement
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
    val prisonerHealth = PrisonerHealth(
      PRISONER_NUMBER,
      SMOKER_OR_VAPER,
      mutableSetOf(EGG_ALLERGY, MILK_ALLERGY),
      mutableSetOf(MEDICAL_DIET_LOW_SALT, MEDICAL_DIET_LOW_FAT),
    )
    save(prisonerHealth)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isEqualTo(SMOKER_OR_VAPER)
      assertThat(foodAllergies).hasSize(2)
      assertThat(foodAllergies).contains(EGG_ALLERGY)
      assertThat(foodAllergies).contains(MILK_ALLERGY)
      assertThat(medicalDietaryRequirements).hasSize(2)
      assertThat(medicalDietaryRequirements).contains(MEDICAL_DIET_LOW_SALT)
      assertThat(medicalDietaryRequirements).contains(MEDICAL_DIET_LOW_FAT)
    }
  }

  @Test
  fun `can persist health with null values`() {
    val prisonerHealth = PrisonerHealth(PRISONER_NUMBER)
    save(prisonerHealth)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isNull()
      assertThat(foodAllergies).isEmpty()
      assertThat(medicalDietaryRequirements).isEmpty()
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
    health.foodAllergies.add(EGG_ALLERGY)
    health.medicalDietaryRequirements.add(MEDICAL_DIET_LOW_SALT)

    repository.save(health)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(smokerOrVaper).isEqualTo(SMOKER_OR_VAPER)
      assertThat(foodAllergies).hasSize(1)
      assertThat(foodAllergies.first()).isEqualTo(EGG_ALLERGY)
      assertThat(medicalDietaryRequirements).hasSize(1)
      assertThat(medicalDietaryRequirements.first()).isEqualTo(MEDICAL_DIET_LOW_SALT)
    }
  }

  @Test
  fun `can test for equality`() {
    assertThat(
      PrisonerHealth(
        PRISONER_NUMBER,
        SMOKER_OR_VAPER,
        mutableSetOf(EGG_ALLERGY),
        mutableSetOf(
          MEDICAL_DIET_LOW_FAT,
        ),
      ),
    ).isEqualTo(
      PrisonerHealth(
        PRISONER_NUMBER,
        SMOKER_OR_VAPER,
        mutableSetOf(EGG_ALLERGY),
        mutableSetOf(MEDICAL_DIET_LOW_FAT),
      ),
    )

    // Prisoner number
    assertThat(PrisonerHealth(PRISONER_NUMBER)).isNotEqualTo(PrisonerHealth("Example"))

    // Smoker/Vaper
    assertThat(PrisonerHealth(PRISONER_NUMBER, SMOKER_OR_VAPER)).isNotEqualTo(
      PrisonerHealth(
        PRISONER_NUMBER,
        SMOKER_NO,
      ),
    )

    // Allergies
    assertThat(PrisonerHealth(PRISONER_NUMBER, SMOKER_OR_VAPER, mutableSetOf(EGG_ALLERGY))).isNotEqualTo(
      PrisonerHealth(
        PRISONER_NUMBER,
        SMOKER_NO,
        mutableSetOf(MILK_ALLERGY),
      ),
    )

    // Medical diet
    assertThat(
      PrisonerHealth(
        PRISONER_NUMBER,
        SMOKER_OR_VAPER,
        mutableSetOf(EGG_ALLERGY),
        mutableSetOf(
          MEDICAL_DIET_LOW_SALT,
        ),
      ),
    ).isNotEqualTo(
      PrisonerHealth(
        PRISONER_NUMBER,
        SMOKER_NO,
        mutableSetOf(EGG_ALLERGY),
        mutableSetOf(MEDICAL_DIET_LOW_FAT),
      ),
    )
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

    val FOOD_ALLERGY_DOMAIN = ReferenceDataDomain("FOOD_ALLERGY", "Food allergy", 0, ZonedDateTime.now(), "OMS_OWNER")
    val EGG_ALLERGY = FoodAllergy(
      prisonerNumber = PRISONER_NUMBER,
      allergy = ReferenceDataCode(
        id = "FOOD_ALLERGY_EGG",
        domain = FOOD_ALLERGY_DOMAIN,
        code = "EGG",
        description = "Egg",
        listSequence = 0,
        createdAt = ZonedDateTime.now(),
        createdBy = "OMS_OWNER",
      ),
    )

    val MILK_ALLERGY =
      FoodAllergy(
        prisonerNumber = PRISONER_NUMBER,
        allergy = ReferenceDataCode(
          id = "FOOD_ALLERGY_MILK",
          domain = FOOD_ALLERGY_DOMAIN,
          code = "MILK",
          description = "Milk",
          listSequence = 0,
          createdAt = ZonedDateTime.now(),
          createdBy = "OMS_OWNER",
        ),
      )

    val MEDICAL_DIET_DOMAIN = ReferenceDataDomain(
      "MEDICAL_DIET",
      "Medical dietary requirements",
      0,
      ZonedDateTime.now(),
      "CONNECT_DPS",
    )

    val MEDICAL_DIET_LOW_FAT =
      MedicalDietaryRequirement(
        prisonerNumber = PRISONER_NUMBER,
        dietaryRequirement = ReferenceDataCode(
          id = "MEDICAL_DIET_LOW_FAT",
          domain = MEDICAL_DIET_DOMAIN,
          code = "LOW_FAT",
          description = "Low fat",
          listSequence = 0,
          createdAt = ZonedDateTime.now(),
          createdBy = "CONNECT_DPS",
        ),
      )

    val MEDICAL_DIET_LOW_SALT =
      MedicalDietaryRequirement(
        prisonerNumber = PRISONER_NUMBER,
        dietaryRequirement = ReferenceDataCode(
          id = "MEDICAL_DIET_LOW_SALT",
          domain = MEDICAL_DIET_DOMAIN,
          code = "LOW_SALT",
          description = "Low salt",
          listSequence = 0,
          createdAt = ZonedDateTime.now(),
          createdBy = "CONNECT_DPS",
        ),
      )

    val SMOKER_OR_VAPER = REF_DATA_CODE
    val SMOKER_NO = ReferenceDataCode(
      id = "SMOKE_NO",
      domain = REF_DATA_DOMAIN,
      code = "NO",
      description = "No, they don't smoke",
      listSequence = 0,
      createdAt = ZonedDateTime.now(),
      createdBy = "OMS_OWNER",
    )
  }
}
