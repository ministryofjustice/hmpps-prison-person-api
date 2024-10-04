package uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers

import io.mockk.clearAllMocks
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.config.EventHandlerException
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType

class EventHandlerFactoryTest {
  private val eventHandler = mock<EventHandler>()

  @AfterEach
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `event handler factory provides expected handler`() {
    whenever(eventHandler.getType()).thenReturn(EVENT_TYPE)
    val eventHandlerFactory = EventHandlerFactory(listOf(eventHandler))

    assertThat(eventHandlerFactory.getHandler(EVENT_TYPE)).isEqualTo(eventHandler)
  }

  @Test
  fun `event handler factory throws when handler not available for event type`() {
    val eventHandlerFactory = EventHandlerFactory(emptyList())

    assertThatThrownBy { eventHandlerFactory.getHandler(EVENT_TYPE) }
      .isInstanceOf(EventHandlerException::class.java)
      .hasMessage("Cannot handle event of type: 'PHYSICAL_ATTRIBUTES_UPDATED'")
  }

  private companion object {
    val EVENT_TYPE = EventType.PHYSICAL_ATTRIBUTES_UPDATED
  }
}
