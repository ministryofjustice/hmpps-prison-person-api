package uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto

data class PrisonerDto(
  val prisonerNumber: String,
  val prisonId: String? = null,
)
