package uk.gov.justice.digital.hmpps.prisonperson.service.event

import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers.EventHandler
import uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers.EventHandlerFactory

class EventServiceTest {

  private val eventHandlerFactory = mock<EventHandlerFactory>()
  private val eventHandler = mock<EventHandler>()
  private val event = mock<PrisonPersonEvent>()

  private lateinit var eventService: EventService

  @BeforeEach
  fun setUp() {
    eventService = EventService(eventHandlerFactory)
  }

  @AfterEach
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `event service delegates to event handler`() {
    whenever(eventHandlerFactory.getHandler(EVENT_TYPE)).thenReturn(eventHandler)
    whenever(event.type).thenReturn(EVENT_TYPE)

    eventService.handleEvent(event)

    verify(eventHandler).handleEvent(event)
  }

  private companion object {
    val EVENT_TYPE = EventType.PHYSICAL_ATTRIBUTES_UPDATED
  }
}
