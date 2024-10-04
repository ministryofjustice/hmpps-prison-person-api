package uk.gov.justice.digital.hmpps.prisonperson.service.event

import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener
import uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers.EventHandlerFactory

@Service
class EventService(private val eventHandlerFactory: EventHandlerFactory) {
  @TransactionalEventListener(phase = AFTER_COMMIT)
  fun handleEvent(event: PrisonPersonEvent) {
    eventHandlerFactory.getHandler(event.type).handleEvent(event)
  }
}
