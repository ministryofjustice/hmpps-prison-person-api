package uk.gov.justice.digital.hmpps.prisonperson.service.event

import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_UPDATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import java.time.ZonedDateTime

abstract class PrisonPersonEvent(val type: EventType) {
  abstract val prisonerNumber: String
  abstract val occurredAt: ZonedDateTime
  abstract val source: Source
  abstract val fields: Collection<PrisonPersonField>

  fun toDomainEvent(baseUrl: String): DomainEvent<PrisonPersonAdditionalInformation> =
    DomainEvent(
      eventType = type.domainEventType,
      additionalInformation = PrisonPersonAdditionalInformation(
        url = "$baseUrl/prisoners/$prisonerNumber",
        prisonerNumber = prisonerNumber,
        source = source,
        fields = fields,
      ),
      description = type.description,
      occurredAt = occurredAt,
    )
}

data class PhysicalAttributesUpdatedEvent(
  override val prisonerNumber: String,
  override val occurredAt: ZonedDateTime,
  override val source: Source,
  override val fields: Collection<PrisonPersonField>,
) : PrisonPersonEvent(PHYSICAL_ATTRIBUTES_UPDATED)
