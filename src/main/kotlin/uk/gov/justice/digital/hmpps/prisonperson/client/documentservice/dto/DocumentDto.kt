package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto

import com.fasterxml.jackson.annotation.JsonCreator

data class DocumentDto @JsonCreator constructor(
  val documentUuid: String,
  val documentType: DocumentType,
  val documentFilename: String,
  val filename: String,
  val fileExtension: String,
  val fileSize: Long,
  val fileHash: String,
  val mimeType: String,
  val metadata: Map<String, String>,
  val createdTime: String,
  val createdByServiceName: String,
  val createdByUsername: String?,
)
