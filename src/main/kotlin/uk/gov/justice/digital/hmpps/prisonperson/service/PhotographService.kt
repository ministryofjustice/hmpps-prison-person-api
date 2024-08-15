package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType

@Service
class PhotographService(private val documentServiceClient: DocumentServiceClient) {
  fun getProfilePicsForPrisoner(prisonerNumber: String, documentRequestContext: DocumentRequestContext): List<DocumentDto> = documentServiceClient.getPhotosForPrisoner(prisonerNumber, documentRequestContext)?.results ?: emptyList()

  fun postProfilePicToDocumentService(
    file: MultipartFile,
    fileType: MediaType,
    prisonerNumber: String,
    documentRequestContext: DocumentRequestContext,
  ) = documentServiceClient.putDocument(
    document = file.bytes,
    filename = file.originalFilename,
    documentType = DocumentType.PRISONER_PROFILE_PICTURE,
    meta = mapOf("prisonerNumber" to prisonerNumber),
    fileType,
    documentRequestContext,
  )
}
