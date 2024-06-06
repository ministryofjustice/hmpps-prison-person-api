package uk.gov.justice.digital.hmpps.prisonperson.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.oauth}") private val authBaseUri: String,
  @Value("\${api.timeout:20s}") val healthTimeout: Duration,
) {

  @Bean
  fun authHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(authBaseUri, healthTimeout)
}
