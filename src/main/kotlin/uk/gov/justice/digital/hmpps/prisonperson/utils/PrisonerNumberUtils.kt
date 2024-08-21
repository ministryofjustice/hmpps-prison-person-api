package uk.gov.justice.digital.hmpps.prisonperson.utils

import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient

fun validatePrisonerNumber(prisonerSearchClient: PrisonerSearchClient, prisonerNumber: String) =
  require(prisonerSearchClient.getPrisoner(prisonerNumber) != null) { "Prisoner number '$prisonerNumber' not found" }
