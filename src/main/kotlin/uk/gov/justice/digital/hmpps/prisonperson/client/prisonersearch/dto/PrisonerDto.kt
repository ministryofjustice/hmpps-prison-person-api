package uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto

import com.fasterxml.jackson.annotation.JsonCreator

data class PrisonerDto @JsonCreator constructor(
  val prisonerNumber: String,
)
