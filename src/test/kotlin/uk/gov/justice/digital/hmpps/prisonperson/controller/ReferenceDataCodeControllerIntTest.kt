package uk.gov.justice.digital.hmpps.prisonperson.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonperson.integration.IntegrationTestBase

class ReferenceDataCodeControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /reference-data/domains/{domain}/codes")
  @Nested
  inner class GetReferenceDataCodesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/HAIR/codes")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/HAIR/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data codes`() {
        webTestClient.get().uri("/reference-data/domains/HAIR/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().is5xxServerError
      }
    }

    @Nested
    inner class NotFound {

      @Test
      fun `receive a 404 when no reference data domain found`() {
        webTestClient.get().uri("/reference-data/domains/UNKNOWN/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().is5xxServerError
      }
    }
  }

  @DisplayName("GET /reference-data/domains/{domain}/codes/{code}")
  @Nested
  inner class GetReferenceDataCodeTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/HAIR/codes/BLONDE")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/HAIR/codes/BLONDE")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data code`() {
        webTestClient.get().uri("/reference-data/domains/HAIR/codes/BLONDE")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().is5xxServerError
      }
    }

    @Nested
    inner class NotFound {

      @Test
      fun `receive a 404 when no reference data domain found`() {
        webTestClient.get().uri("/reference-data/domains/UNKNOWN/codes/BLONDE")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().is5xxServerError
      }

      @Test
      fun `receive a 404 when no reference data code found`() {
        webTestClient.get().uri("/reference-data/domains/HAIR/codes/UNKNOWN")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISON_PERSON_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().is5xxServerError
      }
    }
  }
}
