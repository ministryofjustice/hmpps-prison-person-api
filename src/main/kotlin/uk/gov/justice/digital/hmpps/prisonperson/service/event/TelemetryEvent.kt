package uk.gov.justice.digital.hmpps.prisonperson.service.event

import com.fasterxml.jackson.annotation.JsonCreator

data class TelemetryEvent @JsonCreator constructor(
  val name: String,
  val properties: Map<String, String>?,
)
