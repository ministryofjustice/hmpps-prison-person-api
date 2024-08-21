package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto

enum class DocumentType {
  PRISONER_PROFILE_PICTURE,
  PHYSICAL_IDENTIFIER_PICTURE,
}

enum class OrderBy {
  FILENAME,
  FILE_EXTENSION,
  FILESIZE,
  CREATED_TIME,
}

enum class OrderByDirection {
  ASC,
  DESC,
}

data class DocumentSearchRequestDto(
  val documentType: DocumentType?,
  val metadata: Map<String, String>?,
  val page: Int?,
  val pageSize: Int?,
  val orderBy: OrderBy?,
  val orderByDirection: OrderByDirection?,
)
