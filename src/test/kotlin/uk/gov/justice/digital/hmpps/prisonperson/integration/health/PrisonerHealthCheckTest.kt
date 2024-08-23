package uk.gov.justice.digital.hmpps.prisonperson.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.DocumentServiceExtension.Companion.documentService
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PrisonerSearchExtension.Companion.prisonerSearch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

class PrisonerHealthCheckTest : IntegrationTestBase() {

  @Test
  fun `Health page reports ok`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
      .jsonPath("components.db.status").isEqualTo("UP")
      .jsonPath("components.hmppsAuth.status").isEqualTo("UP")
      .jsonPath("components.prisonerSearch.status").isEqualTo("UP")
      .jsonPath("components.documentService.status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down`() {
    stubPingWithResponse(404)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody()
      .jsonPath("status").isEqualTo("DOWN")
      .jsonPath("components.db.status").isEqualTo("UP")
      .jsonPath("components.hmppsAuth.status").isEqualTo("DOWN")
      .jsonPath("components.prisonerSearch.status").isEqualTo("DOWN")
      .jsonPath("components.documentService.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health info reports version`() {
    stubPingWithResponse(200)

    webTestClient.get().uri("/health")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("components.healthInfo.details.version").value(
        Consumer<String> {
          assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
        },
      )
  }

  @Test
  fun `Health ping page is accessible`() {
    webTestClient.get()
      .uri("/health/ping")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `readiness reports ok`() {
    webTestClient.get()
      .uri("/health/readiness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `liveness reports ok`() {
    webTestClient.get()
      .uri("/health/liveness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Database reports UP`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
      .jsonPath("components.db.status").isEqualTo("UP")
      .jsonPath("components.db.details.database").isEqualTo("PostgreSQL")
  }

  @Test
  fun `HMPPS Domain events topic health reports UP`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
      .jsonPath("components.domainevents-health.status").isEqualTo("UP")
      .jsonPath("components.domainevents-health.details.topicArn").isEqualTo(domainEventsTopic.arn)
      .jsonPath("components.domainevents-health.details.subscriptionsConfirmed").isEqualTo(0)
      .jsonPath("components.domainevents-health.details.subscriptionsPending").isEqualTo(0)
  }

  private fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
    prisonerSearch.stubHealthPing(status)
    documentService.stubHealthPing(status)
  }
}
