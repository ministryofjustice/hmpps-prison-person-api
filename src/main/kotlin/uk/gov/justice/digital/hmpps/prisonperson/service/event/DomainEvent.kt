package uk.gov.justice.digital.hmpps.prisonperson.service.event

import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

data class DomainEvent<T>(
  val eventType: String? = null,
  val additionalInformation: T?,
  val occurredAt: String,
  val description: String,
  val version: String,
) {
  constructor(
    eventType: String,
    additionalInformation: T?,
    occurredAt: ZonedDateTime,
    description: String,
  ) : this(
    eventType,
    additionalInformation,
    occurredAt.toOffsetDateFormat(),
    description,
    "1.0",
  )
}

data class PrisonPersonAdditionalInformation(
  val url: String,
  val source: Source,
  val prisonerNumber: String,
)

data class PrisonerMergedAdditionalInformation(
  val nomsNumber: String,
  val removedNomsNumber: String,
  val reason: String,
)

fun ZonedDateTime.toOffsetDateFormat(): String = ISO_OFFSET_DATE_TIME.format(this)
