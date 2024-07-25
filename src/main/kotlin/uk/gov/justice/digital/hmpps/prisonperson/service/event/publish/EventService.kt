package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener
import uk.gov.justice.digital.hmpps.prisonperson.config.EventProperties

@Service
class EventService(
  private val eventProperties: EventProperties,
  private val telemetryClient: TelemetryClient,
  private val domainEventPublisher: DomainEventPublisher,
) {
  @TransactionalEventListener(phase = AFTER_COMMIT)
  fun handleEvent(event: PrisonPersonEvent) {
    log.info(event.toString())

    event.toDomainEvent(eventProperties.baseUrl).run {
      if (eventProperties.publish) {
        domainEventPublisher.publish(this)

        telemetryClient.trackEvent(
          event.type.telemetryEventName,
          mapOf(
            "prisonerNumber" to event.prisonerNumber,
            "source" to event.source.name,
          ),
          null,
        )
      } else {
        log.info("$eventType event publishing is disabled")
      }
    }
  }

  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
