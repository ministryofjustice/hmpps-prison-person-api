package uk.gov.justice.digital.hmpps.prisonperson.service.event

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.style.RFC4519Style.description
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.DomainEventPublisher
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.ZonedDateTime
import java.util.UUID

class DomainEventPublisherTest {
  private val hmppsQueueService = mock<HmppsQueueService>()
  private val domainEventsTopic = mock<HmppsTopic>()
  private val domainEventsSnsClient = mock<SnsAsyncClient>()
  private val objectMapper = jacksonMapperBuilder().build()

  private val domainEventsTopicArn = "arn:aws:sns:eu-west-2:000000000000:${UUID.randomUUID()}"
  private val baseUrl = "http://localhost:8080"

  @Test
  fun `throws IllegalStateException when topic not found`() {
    whenever(hmppsQueueService.findByTopicId("domainevents")).thenReturn(null)
    val domainEventPublisher = DomainEventPublisher(hmppsQueueService, objectMapper)
    val exception = assertThrows<IllegalStateException> { domainEventPublisher.publish(mock<DomainEvent<Any>>()) }
    assertThat(exception.message).isEqualTo("domainevents not found")
  }

  @Test
  fun `publish physical attributes event`() {
    whenever(hmppsQueueService.findByTopicId("domainevents")).thenReturn(domainEventsTopic)
    whenever(domainEventsTopic.snsClient).thenReturn(domainEventsSnsClient)
    whenever(domainEventsTopic.arn).thenReturn(domainEventsTopicArn)
    val domainEventPublisher = DomainEventPublisher(hmppsQueueService, objectMapper)
    val occurredAt = ZonedDateTime.now()
    val prisonerNumber = "A1234AA"

    val domainEvent = DomainEvent(
      eventType = EVENT_TYPE.domainEventDetails!!.type,
      additionalInformation = PrisonPersonFieldInformation(
        url = "$baseUrl/prisoners/$prisonerNumber",
        prisonerNumber = prisonerNumber,
        source = NOMIS,
        fields = listOf(HEIGHT, WEIGHT),
      ),
      description = EVENT_TYPE.domainEventDetails!!.description,
      occurredAt = occurredAt,
    )

    domainEventPublisher.publish(domainEvent)

    verify(domainEventsSnsClient).publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopic.arn)
        .message(objectMapper.writeValueAsString(domainEvent))
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(domainEvent.eventType)
              .build(),
          ),
        )
        .build(),
    )
  }

  @Test
  fun `publish event - failure`() {
    whenever(hmppsQueueService.findByTopicId("domainevents")).thenReturn(domainEventsTopic)
    whenever(domainEventsTopic.snsClient).thenReturn(domainEventsSnsClient)
    whenever(domainEventsTopic.arn).thenReturn(domainEventsTopicArn)
    val domainEventPublisher = DomainEventPublisher(hmppsQueueService, objectMapper)
    val domainEvent = mock<DomainEvent<Any>>()
    whenever(domainEventsSnsClient.publish(any<PublishRequest>())).thenThrow(RuntimeException("Failed to publish"))

    domainEventPublisher.publish(domainEvent)
  }

  private companion object {
    val EVENT_TYPE = PHYSICAL_ATTRIBUTES_UPDATED
  }
}
