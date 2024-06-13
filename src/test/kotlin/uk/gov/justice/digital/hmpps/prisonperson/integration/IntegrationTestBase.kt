package uk.gov.justice.digital.hmpps.prisonperson.integration

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.prisonperson.config.CLIENT_ID
import uk.gov.justice.digital.hmpps.prisonperson.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PrisonerSearchExtension

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(HmppsAuthApiExtension::class, PrisonerSearchExtension::class)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  internal fun setAuthorisation(
    user: String? = null,
    client: String = CLIENT_ID,
    roles: List<String> = listOf(),
    isUserToken: Boolean = true,
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, client, roles, isUserToken = isUserToken)

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }
}
