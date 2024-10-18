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
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
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
  @Sql("classpath:service/event/physical_attributes.sql")
  @Sql("classpath:service/event/physical_attributes_metadata.sql")
  @Sql("classpath:service/event/physical_attributes_history.sql")
  fun `can handle merge TO a prisoner record with the latest field values`() {
    // ------------------
    // Before the merge:
    // ------------------
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5)
    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).containsExactlyInAnyOrder(-6, -7, -8, -9, -10)
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
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10)
    fieldHistoryRepository.findAllById(listOf(-6, -7, -8, -9, -10)).forEach {
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
  @Sql("classpath:service/event/physical_attributes.sql")
  @Sql("classpath:service/event/physical_attributes_metadata.sql")
  @Sql("classpath:service/event/physical_attributes_history.sql")
  fun `can handle merge FROM a prisoner record with the latest field values`() {
    // ------------------
    // Before the merge:
    // ------------------
    assertThat(fieldHistoryIds(PRISONER1)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5)
    assertThat(fieldHistoryIds(PRISONER2)).containsExactlyInAnyOrder(-6, -7, -8, -9, -10)
    expectFieldMetadata(
      PRISONER2,
      FieldMetadata(PRISONER2, HEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, WEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
    )

    publishPrisonerMergedMessage(PRISONER2, PRISONER1)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER1).size } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(PRISONER2)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10)
    fieldHistoryRepository.findAllById(listOf(-1, -2, -3, -4, -5)).forEach {
      assertThat(it.mergedFrom).isEqualTo(PRISONER1)
      assertThat(it.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    }
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    expectFieldMetadata(
      PRISONER2,
      FieldMetadata(PRISONER2, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER2, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(PRISONER1)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(PRISONER1)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER1)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/event/physical_attributes.sql")
  @Sql("classpath:service/event/physical_attributes_metadata.sql")
  @Sql("classpath:service/event/physical_attributes_history.sql")
  fun `can handle merge TO a prisoner with no prison person data`() {
    // ------------------
    // Before the merge:
    // ------------------
    prisonerSearch.stubGetPrisoner(NO_PRISON_PERSON_DATA)
    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).containsExactlyInAnyOrder(-6, -7, -8, -9, -10)

    publishPrisonerMergedMessage(NO_PRISON_PERSON_DATA, PRISONER_MERGE_FROM)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER_MERGE_FROM).size } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).containsExactlyInAnyOrder(-6, -7, -8, -9, -10)
    fieldHistoryRepository.findAllById(listOf(-6, -7, -8, -9, -10)).forEach {
      assertThat(it.mergedFrom).isEqualTo(PRISONER_MERGE_FROM)
      assertThat(it.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    }
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isNull()
    expectFieldMetadata(
      NO_PRISON_PERSON_DATA,
      FieldMetadata(NO_PRISON_PERSON_DATA, HEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
      FieldMetadata(NO_PRISON_PERSON_DATA, WEIGHT, ZonedDateTime.parse("2024-01-03T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(PRISONER_MERGE_FROM)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(PRISONER_MERGE_FROM)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_MERGE_FROM)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/event/physical_attributes.sql")
  @Sql("classpath:service/event/physical_attributes_metadata.sql")
  @Sql("classpath:service/event/physical_attributes_history.sql")
  fun `can handle merge FROM a prisoner with no prison person data`() {
    // ------------------
    // Before the merge:
    // ------------------
    prisonerSearch.stubGetPrisoner(PRISONER_MERGE_TO)
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5)
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).isEmpty()

    publishPrisonerMergedMessage(PRISONER_MERGE_TO, NO_PRISON_PERSON_DATA)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(PRISONER_MERGE_TO)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5)
    expectFieldMetadata(
      PRISONER_MERGE_TO,
      FieldMetadata(PRISONER_MERGE_TO, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_MERGE_TO, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(NO_PRISON_PERSON_DATA)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(NO_PRISON_PERSON_DATA)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(NO_PRISON_PERSON_DATA)).isEmpty()
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/event/physical_attributes.sql")
  @Sql("classpath:service/event/physical_attributes_metadata.sql")
  @Sql("classpath:service/event/physical_attributes_history.sql")
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
          fields = listOf(HEIGHT, WEIGHT),
        ),
        description = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.description,
        occurredAt = ZonedDateTime.now(clock),
      ),
    )
  }

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:service/event/physical_attributes.sql")
  @Sql("classpath:service/event/physical_attributes_metadata.sql")
  @Sql("classpath:service/event/physical_attributes_history.sql")
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
          "fields" to listOf(HEIGHT, WEIGHT).toString(),
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
  }
}
