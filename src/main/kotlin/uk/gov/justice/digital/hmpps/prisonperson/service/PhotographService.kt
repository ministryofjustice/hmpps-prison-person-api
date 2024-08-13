package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext

@Service
class PhotographService(private val documentServiceClient: DocumentServiceClient) {
  fun getPhotographsForPrisoner(prisonerNumber: String, documentRequestContext: DocumentRequestContext): List<DocumentDto> {
    return documentServiceClient.getPhotosForPrisoner(prisonerNumber, documentRequestContext)?.results ?: emptyList()
  }
}
