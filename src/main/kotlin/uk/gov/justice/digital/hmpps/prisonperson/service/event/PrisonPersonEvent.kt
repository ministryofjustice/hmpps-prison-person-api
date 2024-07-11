package uk.gov.justice.digital.hmpps.prisonperson.service.event

import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

abstract class PrisonPersonEvent(val type: EventType) {
  abstract val prisonerNumber: String
  abstract val occurredAt: ZonedDateTime
  abstract val source: Source

  fun toDomainEvent(baseUrl: String): DomainEvent =
    DomainEvent(
      eventType = type.domainEventType,
      additionalInformation = AdditionalInformation(
        url = "$baseUrl/prisoners/$prisonerNumber",
        prisonerNumber = prisonerNumber,
        source = source,
      ),
      description = type.description,
      occurredAt = ISO_OFFSET_DATE_TIME.format(occurredAt),
      version = 1,
    )
}

data class PhysicalAttributesUpdatedEvent(
  override val prisonerNumber: String,
  override val occurredAt: ZonedDateTime,
  override val source: Source,
) : PrisonPersonEvent(PHYSICAL_ATTRIBUTES_UPDATED)
