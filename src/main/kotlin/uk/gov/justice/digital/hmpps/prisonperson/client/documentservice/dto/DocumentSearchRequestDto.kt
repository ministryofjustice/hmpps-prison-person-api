package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto

import com.fasterxml.jackson.annotation.JsonCreator

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

data class DocumentSearchRequestDto @JsonCreator constructor(
  val documentType: DocumentType?,
  val metadata: Map<String, String>?,
  val page: Int?,
  val pageSize: Int?,
  val orderBy: OrderBy?,
  val orderByDirection: OrderByDirection?,
)
