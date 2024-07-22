package uk.gov.justice.digital.hmpps.prisonperson.service.merge

interface PrisonPersonMerge {
  fun mergeRecords(prisonerNumberFrom: String, prisonerNumberTo: String)
}
