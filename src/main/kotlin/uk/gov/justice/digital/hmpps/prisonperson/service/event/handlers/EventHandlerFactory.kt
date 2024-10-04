package uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonperson.config.EventHandlerException
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import java.util.EnumMap

@Component
class EventHandlerFactory(handlers: Collection<EventHandler>) {
  private val handlers: EnumMap<EventType, EventHandler> = if (handlers.isEmpty()) {
    EnumMap(EventType::class.java)
  } else {
    EnumMap(handlers.associateBy { it.getType() })
  }

  fun getHandler(eventType: EventType): EventHandler = handlers[eventType] ?: throw EventHandlerException(eventType)
}
