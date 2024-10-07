package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener
import uk.gov.justice.digital.hmpps.prisonperson.config.EventProperties

@Service
class DomainEventService(
  private val eventProperties: EventProperties,
  private val domainEventPublisher: DomainEventPublisher,
) {
  @TransactionalEventListener(phase = AFTER_COMMIT)
  fun <T> publishDomainEvent(event: PrisonPersonEvent<T>) {
    event.getDomainEvent(eventProperties.baseUrl)?.run {
      if (eventProperties.publish) {
        log.info("Publishing domain event for $event")
        domainEventPublisher.publish(this)
      } else {
        log.info("$eventType event publishing is disabled")
      }
    }
  }

  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
