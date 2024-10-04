package uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonperson.config.EventProperties
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.DomainEventPublisher

@Component
class PhysicalAttributesUpdatedHandler(
  private val eventProperties: EventProperties,
  private val telemetryClient: TelemetryClient,
  private val domainEventPublisher: DomainEventPublisher,
) : EventHandler {

  override fun getType() = EventType.PHYSICAL_ATTRIBUTES_UPDATED

  override fun handleEvent(event: PrisonPersonEvent) {
    log.info(event.toString())

    event.toDomainEvent(eventProperties.baseUrl).run {
      if (eventProperties.publish) {
        domainEventPublisher.publish(this)

        telemetryClient.trackEvent(
          event.type.telemetryEventName,
          mapOf(
            "prisonerNumber" to event.prisonerNumber,
            "source" to event.source.name,
            "fields" to event.fields.toString(),
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
