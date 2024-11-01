package uk.gov.justice.digital.hmpps.prisonperson.service.merge

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PrisonerSearchExtension.Companion.prisonerSearch
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonFieldInformation
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonerMergedAdditionalInformation
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

class PhysicalAttributesMergeIntTest : IntegrationTestBase() {

  @Autowired
  lateinit var physicalAttributesRepository: PhysicalAttributesRepository

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_metadata.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_history.sql")
  fun `can handle merge TO a prisoner record with the latest field values`() {
    // ------------------
    // Before the merge:
    // ------------------
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrderElementsOf(PRISONER1_FIELD_IDS)
    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).containsExactlyInAnyOrderElementsOf(PRISONER2_FIELD_IDS)
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACIAL_HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, BUILD, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, LEFT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, RIGHT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, SHOE_SIZE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    publishPrisonerMergedMessage(PRISONER_MERGE_TO, PRISONER_MERGE_FROM)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER_MERGE_FROM).size } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrderElementsOf(PRISONER1_FIELD_IDS + PRISONER2_FIELD_IDS)
    fieldHistoryRepository.findAllById(PRISONER2_FIELD_IDS).forEach {
      assertThat(it.mergedFrom).isEqualTo(PRISONER_MERGE_FROM)
      assertThat(it.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    }
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-105).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-205).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-305).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-405).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-505).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-605).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-705).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACIAL_HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, BUILD, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, LEFT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, RIGHT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, SHOE_SIZE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(PRISONER_MERGE_FROM)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_MERGE_FROM)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_metadata.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_history.sql")
  fun `can handle merge FROM a prisoner record with the latest field values`() {
    // ------------------
    // Before the merge:
    // ------------------
    assertThat(fieldHistoryIds(PRISONER1)).containsExactlyInAnyOrderElementsOf(PRISONER1_FIELD_IDS)
    assertThat(fieldHistoryIds(PRISONER2)).containsExactlyInAnyOrderElementsOf(PRISONER2_FIELD_IDS)
    expectFieldMetadata(
      PRISONER2,
      FieldMetadata(PRISONER2, HEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, WEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, HAIR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, FACIAL_HAIR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, FACE, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, BUILD, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, LEFT_EYE_COLOUR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, RIGHT_EYE_COLOUR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, SHOE_SIZE, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
    )

    publishPrisonerMergedMessage(PRISONER2, PRISONER1)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER1).size } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(PRISONER2)).containsExactlyInAnyOrderElementsOf(PRISONER1_FIELD_IDS + PRISONER2_FIELD_IDS)
    fieldHistoryRepository.findAllById(PRISONER1_FIELD_IDS).forEach {
      assertThat(it.mergedFrom).isEqualTo(PRISONER1)
      assertThat(it.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    }
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-105).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-205).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-305).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-405).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-505).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-605).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    assertThat(fieldHistoryRepository.getReferenceById(-705).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    expectFieldMetadata(
      PRISONER2,
      FieldMetadata(PRISONER2, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, HAIR, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, FACIAL_HAIR, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, FACE, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, BUILD, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, LEFT_EYE_COLOUR, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, RIGHT_EYE_COLOUR, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, SHOE_SIZE, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(PRISONER1)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(PRISONER1)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER1)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_metadata.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_history.sql")
  fun `can handle merge TO a prisoner with no prison person data`() {
    // ------------------
    // Before the merge:
    // ------------------
    prisonerSearch.stubGetPrisoner(NO_PRISON_PERSON_DATA)
    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).containsExactlyInAnyOrderElementsOf(PRISONER2_FIELD_IDS)

    publishPrisonerMergedMessage(NO_PRISON_PERSON_DATA, PRISONER_MERGE_FROM)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER_MERGE_FROM).size } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).containsExactlyInAnyOrderElementsOf(PRISONER2_FIELD_IDS)
    fieldHistoryRepository.findAllById(PRISONER2_FIELD_IDS).forEach {
      assertThat(it.mergedFrom).isEqualTo(PRISONER_MERGE_FROM)
      assertThat(it.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    }
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isNull()
    assertThat(fieldHistoryRepository.getReferenceById(-105).appliesTo).isNull()
    assertThat(fieldHistoryRepository.getReferenceById(-205).appliesTo).isNull()
    assertThat(fieldHistoryRepository.getReferenceById(-305).appliesTo).isNull()
    assertThat(fieldHistoryRepository.getReferenceById(-405).appliesTo).isNull()
    assertThat(fieldHistoryRepository.getReferenceById(-505).appliesTo).isNull()
    assertThat(fieldHistoryRepository.getReferenceById(-605).appliesTo).isNull()
    assertThat(fieldHistoryRepository.getReferenceById(-705).appliesTo).isNull()
    expectFieldMetadata(
      NO_PRISON_PERSON_DATA,
      FieldMetadata(NO_PRISON_PERSON_DATA, HEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, WEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, HAIR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, FACIAL_HAIR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, FACE, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, BUILD, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, LEFT_EYE_COLOUR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, RIGHT_EYE_COLOUR, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, SHOE_SIZE, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(PRISONER_MERGE_FROM)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_MERGE_FROM)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_metadata.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_history.sql")
  fun `can handle merge FROM a prisoner with no prison person data`() {
    // ------------------
    // Before the merge:
    // ------------------
    prisonerSearch.stubGetPrisoner(PRISONER_MERGE_TO)
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrderElementsOf(PRISONER1_FIELD_IDS)
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACIAL_HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, BUILD, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, LEFT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, RIGHT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, SHOE_SIZE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).isEmpty()

    publishPrisonerMergedMessage(PRISONER_MERGE_TO, NO_PRISON_PERSON_DATA)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrderElementsOf(PRISONER1_FIELD_IDS)
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACIAL_HAIR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, FACE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, BUILD, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, LEFT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, RIGHT_EYE_COLOUR, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, SHOE_SIZE, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(NO_PRISON_PERSON_DATA)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(NO_PRISON_PERSON_DATA)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/merge/physical_attributes/partial/physical_attributes.sql")
  @Sql("classpath:service/merge/physical_attributes/partial/physical_attributes_metadata.sql")
  @Sql("classpath:service/merge/physical_attributes/partial/physical_attributes_history.sql")
  fun `can handle merges where prisoners have no history for some fields`() {
    // ------------------
    // Before the merge:
    // ------------------
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrderElementsOf(
      PRISONER1_HEIGHT_AND_WEIGHT_FIELD_IDS,
    )
    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).containsExactlyInAnyOrderElementsOf(
      PRISONER2_HEIGHT_AND_WEIGHT_FIELD_IDS,
    )
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    publishPrisonerMergedMessage(PRISONER_MERGE_TO, PRISONER_MERGE_FROM)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER_MERGE_FROM).size } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrderElementsOf(
      PRISONER1_HEIGHT_AND_WEIGHT_FIELD_IDS + PRISONER2_HEIGHT_AND_WEIGHT_FIELD_IDS,
    )
    fieldHistoryRepository.findAllById(PRISONER2_HEIGHT_AND_WEIGHT_FIELD_IDS).forEach {
      assertThat(it.mergedFrom).isEqualTo(PRISONER_MERGE_FROM)
      assertThat(it.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    }
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(PRISONER_MERGE_FROM)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_MERGE_FROM)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_metadata.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_history.sql")
  fun `domain event raised when merge completes`() {
    publishPrisonerMergedMessage(PRISONER_MERGE_TO, PRISONER_MERGE_FROM)

    await untilCallTo { publishTestQueue.countAllMessagesOnQueue() } matches { it == 1 }
    val event = publishTestQueue.receiveDomainEventOnQueue<PrisonPersonFieldInformation>()

    assertThat(event).isEqualTo(
      DomainEvent(
        eventType = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.type,
        additionalInformation = PrisonPersonFieldInformation(
          url = "http://localhost:8080/prisoners/${PRISONER_MERGE_TO}",
          prisonerNumber = PRISONER_MERGE_TO,
          source = DPS,
          fields = listOf(
            HEIGHT,
            WEIGHT,
            HAIR,
            FACIAL_HAIR,
            FACE,
            BUILD,
            LEFT_EYE_COLOUR,
            RIGHT_EYE_COLOUR,
            SHOE_SIZE,
          ),
        ),
        description = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.description,
        occurredAt = ZonedDateTime.now(clock),
      ),
    )
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_metadata.sql")
  @Sql("classpath:service/merge/physical_attributes/full/physical_attributes_history.sql")
  fun `telemetry event raised when merge completes`() {
    publishPrisonerMergedMessage(PRISONER_MERGE_TO, PRISONER_MERGE_FROM)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER_MERGE_FROM).size } matches { it == 0 }

    verify(telemetryClient).trackEvent(
      eq("prison-person-api-physical-attributes-merged"),
      eq(
        mapOf(
          "prisonerNumberFrom" to PRISONER_MERGE_FROM,
          "prisonerNumberTo" to PRISONER_MERGE_TO,
          "source" to DPS.name,
          "fields" to listOf(
            HEIGHT,
            WEIGHT,
            HAIR,
            FACIAL_HAIR,
            FACE,
            BUILD,
            LEFT_EYE_COLOUR,
            RIGHT_EYE_COLOUR,
            SHOE_SIZE,
          ).toString(),
        ),
      ),
      isNull(),
    )
  }

  private fun fieldHistoryIds(prisonerNumber: String) = fieldHistoryRepository.findAllByPrisonerNumber(prisonerNumber)
    .map { it.fieldHistoryId }

  private fun publishPrisonerMergedMessage(prisonerNumber: String, removedPrisonerNumber: String) =
    publishDomainEventMessage(
      eventType = "prison-offender-events.prisoner.merged",
      additionalInformation = PrisonerMergedAdditionalInformation(
        nomsNumber = prisonerNumber,
        removedNomsNumber = removedPrisonerNumber,
        reason = "MERGE",
      ),
      description = "A prisoner has been merged from $removedPrisonerNumber to $prisonerNumber",
    )

  private fun publishDomainEventMessage(
    eventType: String,
    additionalInformation: PrisonerMergedAdditionalInformation,
    description: String,
  ) {
    domainEventsTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopic.arn)
        .message(
          jsonString(
            DomainEvent(
              eventType = eventType,
              additionalInformation = additionalInformation,
              occurredAt = ZonedDateTime.now(clock),
              description = description,
            ),
          ),
        )
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
          ),
        )
        .build(),
    )
  }

  private val awaitAtMost30Secs
    get() = await.atMost(Duration.ofSeconds(30))

  private companion object {
    const val USER1 = "USER1"

    const val PRISONER1 = "A1234AA"
    const val PRISONER2 = "B1234BB"
    const val PRISONER_MERGE_TO = PRISONER1
    const val PRISONER_MERGE_FROM = PRISONER2
    const val NO_PRISON_PERSON_DATA = "Z1234ZZ"

    val PRISONER1_HEIGHT_AND_WEIGHT_FIELD_IDS = listOf<Long>(-1, -2, -3, -4, -5)
    val PRISONER1_FIELD_IDS = PRISONER1_HEIGHT_AND_WEIGHT_FIELD_IDS.plus(
      listOf(
        -101,
        -102,
        -103,
        -201,
        -202,
        -203,
        -301,
        -302,
        -303,
        -401,
        -402,
        -403,
        -501,
        -502,
        -503,
        -601,
        -602,
        -603,
        -701,
        -702,
        -703,
      ),
    )

    val PRISONER2_HEIGHT_AND_WEIGHT_FIELD_IDS = listOf<Long>(-6, -7, -8, -9, -10)
    val PRISONER2_FIELD_IDS = PRISONER2_HEIGHT_AND_WEIGHT_FIELD_IDS.plus(
      listOf(
        -104,
        -105,
        -204,
        -205,
        -304,
        -305,
        -404,
        -405,
        -504,
        -505,
        -604,
        -605,
        -704,
        -705,
      ),
    )
  }
}
