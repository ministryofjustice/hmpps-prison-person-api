package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade

@Service
class PhotographService(private val documentServiceClient: DocumentServiceClient, private val authenticationFacade: AuthenticationFacade) {
  fun getProfilePicsForPrisoner(prisonerNumber: String): List<DocumentDto> =
    documentServiceClient.getPhotosForPrisoner(prisonerNumber, buildDocumentRequestContext())?.results ?: emptyList()

  fun postProfilePicToDocumentService(
    file: MultipartFile,
    fileType: MediaType,
    prisonerNumber: String,
  ) = documentServiceClient.putDocument(
    document = file.bytes,
    filename = file.originalFilename,
    documentType = DocumentType.PRISONER_PROFILE_PICTURE,
    meta = mapOf("prisonerNumber" to prisonerNumber),
    fileType,
    documentRequestContext = buildDocumentRequestContext(),
  )
  fun getPicByUuid(uuid: String): Pair<ByteArray?, String?> = documentServiceClient.getDocumentByUuid(uuid, buildDocumentRequestContext())

  private fun buildDocumentRequestContext() = DocumentRequestContext(
    serviceName = "hmpps-prison-person-api",
    username = authenticationFacade.getUserOrSystemInContext(),
  )
}
