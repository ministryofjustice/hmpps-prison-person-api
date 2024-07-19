package uk.gov.justice.digital.hmpps.prisonperson.service.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.PhysicalAttributesRepository
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

class DomainEventListenerIntTest : IntegrationTestBase() {

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
  fun `can merge prison person data`() {
    // ------------------
    // Before the merge:
    // ------------------
    assertThat(fieldHistoryIds(PRISONER_NUMBER)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5)
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isNull()
    assertThat(fieldHistoryIds(REMOVED_PRISONER_NUMBER)).containsExactlyInAnyOrder(-6, -7, -8, -9, -10)
    expectFieldMetadata(
      PRISONER_NUMBER,
      FieldMetadata(PRISONER_NUMBER, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_NUMBER, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    publishPrisonerMergedMessage(PRISONER_NUMBER, REMOVED_PRISONER_NUMBER)
    awaitAtMost30Secs untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
    awaitAtMost30Secs untilCallTo { fieldHistoryIds(PRISONER_NUMBER).size } matches { it == 10 }

    // -----------------
    // After the merge:
    // -----------------
    assertThat(fieldHistoryIds(PRISONER_NUMBER)).containsExactlyInAnyOrder(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10)
    fieldHistoryRepository.findAllById(listOf(-6, -7, -8, -9, -10)).forEach {
      assertThat(it.mergedFrom).isEqualTo(REMOVED_PRISONER_NUMBER)
      assertThat(it.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    }
    assertThat(fieldHistoryRepository.getReferenceById(-10).appliesTo).isEqualTo(ZonedDateTime.now(clock))
    expectFieldMetadata(
      PRISONER_NUMBER,
      FieldMetadata(PRISONER_NUMBER, HEIGHT, ZonedDateTime.parse("2024-01-10T00:00:00+00:00"), USER1),
      FieldMetadata(PRISONER_NUMBER, WEIGHT, ZonedDateTime.parse("2024-01-05T00:00:00+00:00"), USER1),
    )

    assertThat(fieldHistoryIds(REMOVED_PRISONER_NUMBER)).isEmpty()
    assertThat(physicalAttributesRepository.findByIdOrNull(REMOVED_PRISONER_NUMBER)).isNull()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(REMOVED_PRISONER_NUMBER)).isEmpty()
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
    const val PRISONER_NUMBER = "A1234AA"
    const val REMOVED_PRISONER_NUMBER = "B1234BB"
  }
}
