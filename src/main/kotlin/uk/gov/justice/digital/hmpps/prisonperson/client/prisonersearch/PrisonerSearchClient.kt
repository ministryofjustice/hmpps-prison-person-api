package uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.prisonperson.config.DownstreamServiceException

@Component
class PrisonerSearchClient(@Qualifier("prisonerSearchWebClient") private val webClient: WebClient) {
  fun getPrisoner(prisonerNumber: String): PrisonerDto? {
    return try {
      webClient
        .get()
        .uri("/prisoner/{prisonerNumber}", prisonerNumber)
        .retrieve()
        .bodyToMono(PrisonerDto::class.java)
        .block()
    } catch (e: NotFound) {
      null
    } catch (e: Exception) {
      throw DownstreamServiceException("Get prisoner request failed", e)
    }
  }
}
