package uk.gov.justice.digital.hmpps.prisonperson.service.event

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.prisonperson.config.EventProperties
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers.PhysicalAttributesUpdatedHandler
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.DomainEventPublisher
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class PhysicalAttributesUpdatedHandlerTest {

  private val telemetryClient = mock<TelemetryClient>()
  private val domainEventPublisher = mock<DomainEventPublisher>()
  private val baseUrl = "http://localhost:8080"

  @Test
  fun `handle event - publish enabled`() {
    val eventProperties = EventProperties(publish = true, baseUrl = baseUrl)
    val handler = PhysicalAttributesUpdatedHandler(eventProperties, telemetryClient, domainEventPublisher)
    val event = PhysicalAttributesUpdatedEvent(PRISONER_NUMBER, NOW, DPS, listOf(HEIGHT, WEIGHT))

    handler.handleEvent(event)

    val domainEventCaptor = argumentCaptor<DomainEvent<*>>()
    verify(domainEventPublisher).publish(domainEventCaptor.capture())
    assertThat(domainEventCaptor.firstValue).isEqualTo(
      DomainEvent(
        eventType = PHYSICAL_ATTRIBUTES_UPDATED.domainEventType,
        additionalInformation = PrisonPersonAdditionalInformation(
          url = "$baseUrl/prisoners/$PRISONER_NUMBER",
          source = event.source,
          prisonerNumber = PRISONER_NUMBER,
          fields = listOf(HEIGHT, WEIGHT),
        ),
        description = PHYSICAL_ATTRIBUTES_UPDATED.description,
        occurredAt = NOW,
      ),
    )
  }

  @Test
  fun `handle event - publish disabled`() {
    val eventProperties = EventProperties(publish = false, baseUrl = baseUrl)
    val handler = PhysicalAttributesUpdatedHandler(eventProperties, telemetryClient, domainEventPublisher)
    val event = PhysicalAttributesUpdatedEvent(PRISONER_NUMBER, NOW, DPS, listOf(HEIGHT, WEIGHT))

    handler.handleEvent(event)

    verify(domainEventPublisher, never()).publish(any<DomainEvent<*>>())
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

    val NOW: ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/London"))
  }
}
