package uk.gov.justice.digital.hmpps.prisonperson.service.event.handlers

import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonEvent

interface EventHandler {
  fun handleEvent(event: PrisonPersonEvent)
  fun getType(): EventType
}
