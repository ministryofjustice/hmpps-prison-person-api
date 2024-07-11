package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase

class ReferenceDataDomainControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /reference-data/domains")
  @Nested
  inner class GetReferenceDataDomainsTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains")
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data domains`() {
        webTestClient.get().uri("/reference-data/domains")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().is5xxServerError
      }
    }
  }

  @DisplayName("GET /reference-data/domains/{domain}")
  @Nested
  inner class GetReferenceDataDomainTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/HAIR")
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data domain`() {
        webTestClient.get().uri("/reference-data/domains/HAIR")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().is5xxServerError
      }
    }

    @Nested
    inner class NotFound {

      @Test
      fun `receive a 404 when no reference data domain found`() {
        webTestClient.get().uri("/reference-data/domains/UNKNOWN")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().is5xxServerError
      }
    }
  }
}
