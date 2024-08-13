package uk.gov.justice.digital.hmpps.prisonperson.client.documentservice

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchRequestDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchResponseDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderBy
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderByDirection
import uk.gov.justice.digital.hmpps.prisonperson.config.DownstreamServiceException

@Component
class DocumentServiceClient(@Qualifier("documentServiceWebClient") private val webClient: WebClient) {
  fun getPhotosForPrisoner(prisonerNumber: String, documentRequestContext: DocumentRequestContext): DocumentSearchResponseDto? {
    val requestBody = DocumentSearchRequestDto(
      documentType = DocumentType.PRISONER_PROFILE_PICTURE,
      metadata = mapOf("prisonerNumber" to prisonerNumber),
      page = 0,
      pageSize = 50,
      orderBy = OrderBy.CREATED_TIME,
      orderByDirection = OrderByDirection.DESC,
    )

    return try {
      val request = webClient
        .post()
        .uri("/documents/search")
        .header("Service-Name", documentRequestContext.serviceName)

      if (!documentRequestContext.activeCaseLoadId.isNullOrEmpty()) {
        request.header("Active-Case-Load-Id", documentRequestContext.activeCaseLoadId)
      }

      if (!documentRequestContext.username.isNullOrEmpty()) {
        request.header("Username", documentRequestContext.username)
      }

      request
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .bodyToMono(DocumentSearchResponseDto::class.java)
        .block()
    } catch (e: Exception) {
      throw DownstreamServiceException("Post document search request failed", e)
    }
  }
}
