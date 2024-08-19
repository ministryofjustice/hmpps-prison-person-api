package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto

data class DocumentRequestContext(
  val serviceName: String,
  val activeCaseLoadId: String? = null,
  val username: String? = null,
)
