package uk.gov.justice.digital.hmpps.prisonperson.service.event

data class TelemetryEvent(
  val name: String,
  val properties: Map<String, String>,
)
