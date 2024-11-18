package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.publish

@Service
class DomainEventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("domainevents") ?: throw IllegalStateException("domainevents not found")
  }

  fun <T> publish(domainEvent: DomainEvent<T>) =
    domainEventsTopic.publish(domainEvent.eventType!!, objectMapper.writeValueAsString(domainEvent))
}
