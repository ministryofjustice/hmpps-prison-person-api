package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonAdditionalInformation
import java.time.ZonedDateTime

abstract class PrisonPersonEvent(val type: EventType) {
  abstract val prisonerNumber: String
  abstract val occurredAt: ZonedDateTime
  abstract val source: Source

  fun toDomainEvent(baseUrl: String): DomainEvent<PrisonPersonAdditionalInformation> =
    DomainEvent(
      eventType = type.domainEventType,
      additionalInformation = PrisonPersonAdditionalInformation(
        url = "$baseUrl/prisoners/$prisonerNumber",
        prisonerNumber = prisonerNumber,
        source = source,
      ),
      description = type.description,
      occurredAt = occurredAt,
    )
}

data class PhysicalAttributesUpdatedEvent(
  override val prisonerNumber: String,
  override val occurredAt: ZonedDateTime,
  override val source: Source,
) : PrisonPersonEvent(PHYSICAL_ATTRIBUTES_UPDATED)
