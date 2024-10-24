package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto

import com.fasterxml.jackson.annotation.JsonCreator

data class DocumentRequestContext @JsonCreator constructor(
  val serviceName: String,
  val activeCaseLoadId: String? = null,
  val username: String? = null,
)
