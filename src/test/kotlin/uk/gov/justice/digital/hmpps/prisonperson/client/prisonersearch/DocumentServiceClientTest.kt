package uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchRequestDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchResponseDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderBy
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderByDirection
import uk.gov.justice.digital.hmpps.prisonperson.config.DownstreamServiceException
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.DocumentServiceServer
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PRISONER_NUMBER
import java.util.UUID

class DocumentServiceClientTest {
  private lateinit var client: DocumentServiceClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = DocumentServiceClient(webClient)
  }

  @Nested
  inner class GetPhotosForPrisoner {
    @Test
    fun `getPhotosForPrisoner - success`() {
      server.stubGetAllPicturesForPrisoner()

      val result = client.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)

      assertThat(result!!).isEqualTo(
        DocumentSearchResponseDto(
          request = DocumentSearchRequestDto(
            documentType = DocumentType.PRISONER_PROFILE_PICTURE,
            metadata = mapOf("prisonerNumber" to PRISONER_NUMBER),
            page = 0,
            pageSize = 10,
            orderBy = OrderBy.CREATED_TIME,
            orderByDirection = OrderByDirection.DESC,
          ),
          results = emptyList(),
          totalResultsCount = 0,
        ),
      )
    }

    @Test
    fun `getPhotosForPrisoner - headers are set for document request`() {
      server.stubGetAllPicturesForPrisoner()

      client.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)

      server.verify(
        postRequestedFor(urlEqualTo("/documents/search"))
          .withHeader("Service-Name", equalTo("hmpps-prisoner-profile"))
          .withHeader("Active-Case-Load-Id", equalTo("MDI"))
          .withHeader("Username", equalTo("USERNAME")),
      )
    }

    @Test
    fun `getPhotosForPrisoner - active case load and username headers are optional`() {
      server.stubGetAllPicturesForPrisoner()

      client.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT_JUST_SERVICE)

      server.verify(
        postRequestedFor(urlEqualTo("/documents/search"))
          .withHeader("Service-Name", equalTo("hmpps-prisoner-profile")),
      )
    }

    @Test
    fun `getPhotosForPrisoner - downstream service exception`() {
      server.stubGetAllPicturesForPrisonerException()

      assertThatThrownBy { client.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT) }
        .isInstanceOf(DownstreamServiceException::class.java)
        .hasMessage("Post document search request failed")
        .hasCauseInstanceOf(WebClientResponseException::class.java)
        .hasRootCauseMessage("500 Internal Server Error from POST http://localhost:8113/documents/search")
    }
  }

  @Nested
  inner class PutDocument {
    @BeforeEach
    fun setup() {
      mockkStatic(UUID::class)
      val mockUuid = UUID.randomUUID().toString()
      every { UUID.randomUUID().toString() } returns mockUuid
    }

    @AfterEach
    fun teardown() {
      unmockkStatic(UUID::class)
    }

    @Test
    fun `putDocument - success`() {
      val mockUuid = UUID.randomUUID().toString()
      server.stubPostNewDocument(uuid = mockUuid, result = DOCUMENT_DTO)

      val result = client.putDocument(
        "mock content".toByteArray(),
        "filename.jpg",
        DocumentType.PRISONER_PROFILE_PICTURE,
        mapOf(),
        MediaType.IMAGE_JPEG,
        DOCUMENT_REQ_CONTEXT,
      )

      assertThat(result).isEqualTo(
        DOCUMENT_DTO,
      )
    }

    @Test
    fun `putDocument - downstream service exception`() {
      val uuid = UUID.randomUUID().toString()
      server.stubPostNewDocumentException(documentType = DocumentType.PRISONER_PROFILE_PICTURE, uuid = uuid)

      assertThatThrownBy {
        client.putDocument(
          "mock content".toByteArray(),
          "filename.jpg",
          DocumentType.PRISONER_PROFILE_PICTURE,
          mapOf(),
          MediaType.IMAGE_JPEG,
          DOCUMENT_REQ_CONTEXT,
        )
      }
        .isInstanceOf(DownstreamServiceException::class.java)
        .hasMessage("Put document request failed")
        .hasCauseInstanceOf(WebClientResponseException::class.java)
        .hasRootCauseMessage("500 Internal Server Error from POST http://localhost:8113/documents/PRISONER_PROFILE_PICTURE/$uuid")
    }
  }

  companion object {
    @JvmField
    internal val server = DocumentServiceServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      server.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      server.stop()
    }

    val DOCUMENT_REQ_CONTEXT = DocumentRequestContext(
      serviceName = "hmpps-prisoner-profile",
      activeCaseLoadId = "MDI",
      username = "USERNAME",
    )

    val DOCUMENT_REQ_CONTEXT_JUST_SERVICE = DocumentRequestContext(
      serviceName = "hmpps-prisoner-profile",
    )

    val DOCUMENT_DTO = DocumentDto(
      documentUuid = "abc",
      documentType = DocumentType.PRISONER_PROFILE_PICTURE,
      documentFilename = "fileName",
      filename = "fileName",
      fileExtension = "jpg",
      fileSize = 80,
      fileHash = "hash",
      mimeType = "mime",
      metadata = mapOf("prisonerNumber" to "A1234AA"),
      createdTime = "2021-01-01T00:00:00",
      createdByServiceName = "service",
      createdByUsername = "user",
    )
  }
}
