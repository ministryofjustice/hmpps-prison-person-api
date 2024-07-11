package uk.gov.justice.digital.hmpps.prisonperson.service.event

import uk.gov.justice.digital.hmpps.prisonperson.enums.Source

data class DomainEvent(
  val eventType: String,
  val additionalInformation: AdditionalInformation,
  val version: Int,
  val description: String,
  val occurredAt: String,
)

data class AdditionalInformation(
  val url: String,
  val source: Source,
  val prisonerNumber: String,
)
