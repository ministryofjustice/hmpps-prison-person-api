package uk.gov.justice.digital.hmpps.prisonperson.service.event

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
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.DomainEventPublisher
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.DomainEventService
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.PrisonPersonUpdatedEvent
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class DomainEventServiceTest {

  private val domainEventPublisher = mock<DomainEventPublisher>()

  private val baseUrl = "http://localhost:8080"

  @Test
  fun `handle event - publish enabled`() {
    val eventProperties = EventProperties(publish = true, baseUrl = baseUrl)
    val domainEventService = DomainEventService(eventProperties, domainEventPublisher)

    domainEventService.publishDomainEvent(EVENT)

    val domainEventCaptor = argumentCaptor<DomainEvent<*>>()
    verify(domainEventPublisher).publish(domainEventCaptor.capture())
    assertThat(domainEventCaptor.firstValue).isEqualTo(
      DomainEvent(
        eventType = EVENT_TYPE.domainEventDetails!!.type,
        additionalInformation = PrisonPersonFieldInformation(
          url = "$baseUrl/prisoners/$PRISONER_NUMBER",
          source = SOURCE,
          prisonerNumber = PRISONER_NUMBER,
          fields = FIELDS,
        ),
        description = PHYSICAL_ATTRIBUTES_UPDATED.domainEventDetails!!.description,
        occurredAt = NOW,
      ),
    )
  }

  @Test
  fun `handle event - publish disabled`() {
    val eventProperties = EventProperties(publish = false, baseUrl = baseUrl)
    val domainEventService = DomainEventService(eventProperties, domainEventPublisher)

    domainEventService.publishDomainEvent(EVENT)

    verify(domainEventPublisher, never()).publish(any<DomainEvent<*>>())
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

    val NOW: ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/London"))
    val EVENT_TYPE = PHYSICAL_ATTRIBUTES_UPDATED
    val FIELDS = listOf(HEIGHT, WEIGHT)
    val SOURCE = DPS
    val EVENT = PrisonPersonUpdatedEvent(EVENT_TYPE, PRISONER_NUMBER, NOW, SOURCE, FIELDS)
  }
}
