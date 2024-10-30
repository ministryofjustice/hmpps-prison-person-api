package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonPersonFieldInformation
import uk.gov.justice.digital.hmpps.prisonperson.service.event.TelemetryEvent
import java.time.ZonedDateTime

interface PrisonPersonEvent<T> {
  val eventType: EventType
  val prisonerNumber: String

  fun getDomainEvent(baseUrl: String): DomainEvent<T>? = null
  fun getTelemetryEvent(): TelemetryEvent? = null
}

data class PrisonPersonUpdatedEvent(
  override val eventType: EventType,
  override val prisonerNumber: String,
  val occurredAt: ZonedDateTime,
  val source: Source,
  val fields: Collection<PrisonPersonField>,
  val fieldHistoryIds: Collection<Long>? = null,
) : PrisonPersonEvent<PrisonPersonFieldInformation> {
  override fun getDomainEvent(baseUrl: String): DomainEvent<PrisonPersonFieldInformation>? =
    eventType.domainEventDetails?.let {
      DomainEvent(
        eventType = it.type,
        additionalInformation = PrisonPersonFieldInformation(
          url = "$baseUrl/prisoners/$prisonerNumber",
          prisonerNumber = prisonerNumber,
          source = source,
          fields = fields,
        ),
        description = it.description,
        occurredAt = occurredAt,
      )
    }

  override fun getTelemetryEvent(): TelemetryEvent =
    TelemetryEvent(
      eventType.telemetryEventDetails!!.name,
      mapOf(
        "prisonerNumber" to prisonerNumber,
        "source" to source.name,
        "fields" to fields.toString(),
        "fieldHistoryIds" to fieldHistoryIds.toString(),
      ),
    )
}

data class PrisonPersonMergedEvent(
  override val eventType: EventType,
  val prisonerNumberFrom: String,
  val prisonerNumberTo: String,
  val occurredAt: ZonedDateTime,
  val source: Source,
  val fields: Collection<PrisonPersonField>,
) : PrisonPersonEvent<PrisonPersonFieldInformation> {
  override val prisonerNumber: String
    get() = prisonerNumberTo

  override fun getDomainEvent(baseUrl: String): DomainEvent<PrisonPersonFieldInformation>? =
    eventType.domainEventDetails?.let {
      DomainEvent(
        eventType = it.type,
        additionalInformation = PrisonPersonFieldInformation(
          url = "$baseUrl/prisoners/$prisonerNumber",
          prisonerNumber = prisonerNumber,
          source = source,
          fields = fields,
        ),
        description = it.description,
        occurredAt = occurredAt,
      )
    }

  override fun getTelemetryEvent(): TelemetryEvent =
    TelemetryEvent(
      eventType.telemetryEventDetails!!.name,
      mapOf(
        "prisonerNumberFrom" to prisonerNumberFrom,
        "prisonerNumberTo" to prisonerNumberTo,
        "source" to source.name,
        "fields" to fields.toString(),
      ),
    )
}
