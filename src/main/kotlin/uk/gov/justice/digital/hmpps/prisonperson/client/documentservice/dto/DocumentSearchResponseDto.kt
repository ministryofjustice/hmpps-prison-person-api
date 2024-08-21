package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto

data class DocumentSearchResponseDto(
  val request: DocumentSearchRequestDto,
  val results: List<DocumentDto>,
  val totalResultsCount: Long,
)
