@file:Suppress("ktlint:standard:filename")

package uk.gov.justice.digital.hmpps.prisonperson.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

@Component("hmppsAuth")
class HmppsAuthApiHealth(@Qualifier("authHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonerSearch")
class PrisonerSearchHealthPingCheck(@Qualifier("prisonerSearchHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("documentService")
class DocumentServiceHealthPingCheck(@Qualifier("documentServiceHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)
