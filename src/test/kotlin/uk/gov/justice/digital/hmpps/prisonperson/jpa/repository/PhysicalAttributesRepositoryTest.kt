package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
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
      HAIR,
      FACIAL_HAIR,
      FACE,
      BUILD,
      LEFT_EYE_COLOUR,
      RIGHT_EYE_COLOUR,
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
      assertThat(hair).isNull()
      assertThat(facialHair).isNull()
      assertThat(face).isNull()
      assertThat(build).isNull()
      assertThat(leftEyeColour).isNull()
      assertThat(rightEyeColour).isNull()
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
    physicalAttributes.hair = HAIR
    physicalAttributes.facialHair = FACIAL_HAIR
    physicalAttributes.face = FACE
    physicalAttributes.build = BUILD
    physicalAttributes.leftEyeColour = LEFT_EYE_COLOUR
    physicalAttributes.rightEyeColour = RIGHT_EYE_COLOUR

    repository.save(physicalAttributes)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(height).isEqualTo(PRISONER_HEIGHT)
      assertThat(weight).isEqualTo(PRISONER_WEIGHT)
      assertThat(hair).isEqualTo(HAIR)
      assertThat(facialHair).isEqualTo(FACIAL_HAIR)
      assertThat(face).isEqualTo(FACE)
      assertThat(build).isEqualTo(BUILD)
      assertThat(leftEyeColour).isEqualTo(LEFT_EYE_COLOUR)
      assertThat(rightEyeColour).isEqualTo(RIGHT_EYE_COLOUR)
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

    val REF_DATA_DOMAIN = ReferenceDataDomain("FACE", "Face shape", 0, ZonedDateTime.now(), "OMS_OWNER")
    val REF_DATA_CODE = ReferenceDataCode(
      id = "FACE_OVAL",
      domain = REF_DATA_DOMAIN,
      code = "OVAL",
      description = "Oval",
      listSequence = 0,
      createdAt = ZonedDateTime.now(),
      createdBy = "OMS_OWNER",
    )

    val HAIR = REF_DATA_CODE
    val FACIAL_HAIR = REF_DATA_CODE
    val FACE = REF_DATA_CODE
    val BUILD = REF_DATA_CODE
    val LEFT_EYE_COLOUR = REF_DATA_CODE
    val RIGHT_EYE_COLOUR = REF_DATA_CODE
  }
}
