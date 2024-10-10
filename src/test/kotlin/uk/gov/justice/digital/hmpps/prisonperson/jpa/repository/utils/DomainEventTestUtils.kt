package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.jpa.PhysicalAttributes
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.PrisonPersonEvent

fun PhysicalAttributes.expectDomainEventRaised(
  prisonerNumber: String,
  eventType: EventType,
  customCheck: ((PrisonPersonEvent<*>) -> Unit)? = null,
) {
  assertThat(domainEvents()).hasSize(1)
  with(domainEvents().first() as PrisonPersonEvent<*>) {
    assertThat(this.prisonerNumber).isEqualTo(prisonerNumber)
    assertThat(this.eventType).isEqualTo(eventType)
    customCheck?.invoke(this)
  }
}
