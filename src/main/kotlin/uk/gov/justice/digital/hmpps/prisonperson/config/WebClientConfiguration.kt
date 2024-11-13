package uk.gov.justice.digital.hmpps.prisonperson.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.oauth.base.url}") private val authBaseUri: String,
  @Value("\${api.oauth.health.timeout:20s}") private val authHealthTimeout: Duration,

  @Value("\${api.prisoner-search.base.url}") private val prisonerSearchBaseUri: String,
  @Value("\${api.prisoner-search.timeout:30s}") private val prisonerSearchTimeout: Duration,
  @Value("\${api.prisoner-search.health.timeout:20s}") private val prisonerSearchHealthTimeout: Duration,

  @Value("\${api.document-service.base.url}") private val documentServiceBaseUri: String,
  @Value("\${api.document-service.timeout:30s}") private val documentServiceTimeout: Duration,
  @Value("\${api.document-service.health.timeout:20s}") private val documentServiceHealthTimeout: Duration,
) {

  @Bean
  fun authHealthWebClient(builder: WebClient.Builder) = builder.healthWebClient(authBaseUri, authHealthTimeout)

  @Bean
  fun prisonerSearchHealthWebClient(builder: WebClient.Builder) =
    builder.healthWebClient(prisonerSearchBaseUri, prisonerSearchHealthTimeout)

  @Bean
  fun prisonerSearchWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: WebClient.Builder) =
    builder.authorisedWebClient(authorizedClientManager, "hmpps-prison-person-api", prisonerSearchBaseUri, prisonerSearchTimeout)

  @Bean
  fun documentServiceHealthWebClient(builder: WebClient.Builder) =
    builder.healthWebClient(documentServiceBaseUri, documentServiceHealthTimeout)

  @Bean
  fun documentServiceWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: WebClient.Builder) =
    builder.authorisedWebClient(authorizedClientManager, "hmpps-prison-person-api", documentServiceBaseUri, documentServiceTimeout)
}
