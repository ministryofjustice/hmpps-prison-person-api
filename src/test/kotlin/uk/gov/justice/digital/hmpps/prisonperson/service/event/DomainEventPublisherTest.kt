package uk.gov.justice.digital.hmpps.prisonperson.service.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.openid.connect.sdk.claims.LogoutTokenClaimsSet.EVENT_TYPE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.style.RFC4519Style.description
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.NOMIS
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.DomainEventPublisher
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.publish
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.supplyAsync

class DomainEventPublisherTest {
  private val hmppsQueueService = mock<HmppsQueueService>()
  private val domainEventsSnsClient = mock<SnsAsyncClient>()
  private val objectMapper = mock<ObjectMapper>()
  private val publishResponse = mock<PublishResponse>()
  private val domainEventPublisher = DomainEventPublisher(hmppsQueueService, objectMapper)

  @Test
  fun `throws IllegalStateException when topic not found`() {
    whenever(hmppsQueueService.findByTopicId(TOPIC_ID)).thenReturn(null)
    val exception = assertThrows<IllegalStateException> { domainEventPublisher.publish(mock<DomainEvent<Any>>()) }
    assertThat(exception.message).isEqualTo("$TOPIC_ID not found")
  }

  @Test
  fun `publish physical attributes event`() {
    whenever(objectMapper.writeValueAsString(any())).thenReturn(DOMAIN_EVENT_STRING)
    whenever(hmppsQueueService.findByTopicId(TOPIC_ID)).thenReturn(HmppsTopic("id", TOPIC_ARN, domainEventsSnsClient))
    whenever(domainEventsSnsClient.publish(any<PublishRequest>())).thenReturn(completedFuture(publishResponse))

    domainEventPublisher.publish(DOMAIN_EVENT)

    verify(domainEventsSnsClient).publish(
      PublishRequest.builder().message(DOMAIN_EVENT_STRING)
        .topicArn(TOPIC_ARN)
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder()
              .dataType("String")
              .stringValue(EVENT_TYPE.domainEventDetails!!.type)
              .build(),
          ),
        )
        .build(),
    )
  }

  @Test
  fun `publish event - failed events are retried`() {
    whenever(objectMapper.writeValueAsString(any())).thenReturn(DOMAIN_EVENT_STRING)
    whenever(hmppsQueueService.findByTopicId(TOPIC_ID)).thenReturn(HmppsTopic("id", TOPIC_ARN, domainEventsSnsClient))
    whenever(domainEventsSnsClient.publish(any<PublishRequest>()))
      .thenReturn(supplyAsync { throw RuntimeException("error") })
      .thenReturn(completedFuture(publishResponse))

    domainEventPublisher.publish(DOMAIN_EVENT)

    verify(domainEventsSnsClient, times(2)).publish(
      PublishRequest.builder().message(DOMAIN_EVENT_STRING)
        .topicArn(TOPIC_ARN)
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder()
              .dataType("String")
              .stringValue(EVENT_TYPE.domainEventDetails!!.type)
              .build(),
          ),
        )
        .build(),
    )
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val BASE_URL = "http://localhost:8080"
    const val TOPIC_ID = "domainevents"
    const val TOPIC_ARN = "topicArn"
    const val DOMAIN_EVENT_STRING = "messageAsJson"

    val EVENT_TYPE = PHYSICAL_ATTRIBUTES_UPDATED
    val DOMAIN_EVENT = DomainEvent(
      eventType = EVENT_TYPE.domainEventDetails!!.type,
      additionalInformation = PrisonPersonFieldInformation(
        url = "$BASE_URL/prisoners/$PRISONER_NUMBER",
        prisonerNumber = PRISONER_NUMBER,
        source = NOMIS,
        fields = listOf(HEIGHT, WEIGHT),
      ),
      description = EVENT_TYPE.domainEventDetails!!.description,
      occurredAt = ZonedDateTime.now(),
    )
  }
}
