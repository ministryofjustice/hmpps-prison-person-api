package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto

import com.fasterxml.jackson.annotation.JsonCreator

data class DocumentSearchResponseDto @JsonCreator constructor(
  val request: DocumentSearchRequestDto,
  val results: List<DocumentDto>,
  val totalResultsCount: Long,
)
