package uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchRequestDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchResponseDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderBy
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderByDirection
import java.nio.file.Files
import java.nio.file.Paths

class DocumentServiceServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8113
  }

  private val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) """{"status":"UP"}""" else """{"status":"DOWN"}""")
          .withStatus(status),
      ),
    )
  }

  fun stubGetAllPicturesForPrisoner(results: List<DocumentDto> = emptyList(), prisonerNumber: String = PRISONER_NUMBER): StubMapping =
    stubFor(
      post("/documents/search")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(
                DocumentSearchResponseDto(
                  request = DocumentSearchRequestDto(
                    documentType = DocumentType.PRISONER_PROFILE_PICTURE,
                    metadata = mapOf("prisonerNumber" to prisonerNumber),
                    page = 0,
                    pageSize = 10,
                    orderBy = OrderBy.CREATED_TIME,
                    orderByDirection = OrderByDirection.DESC,
                  ),
                  results = results,
                  totalResultsCount = 0,
                ),
              ),
            )
            .withStatus(200),
        ),
    )

  fun stubGetAllPicturesForPrisonerException(): StubMapping =
    stubFor(post("/documents/search").willReturn(aResponse().withStatus(500)))

  fun stubPostNewDocument(uuid: String = "xxx", documentType: DocumentType = DocumentType.PRISONER_PROFILE_PICTURE, result: DocumentDto?): StubMapping =
    stubFor(
      post("/documents/$documentType/$uuid")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(result),
            )
            .withStatus(200),
        ),
    )

  fun stubPostNewDocumentException(documentType: DocumentType = DocumentType.PRISONER_PROFILE_PICTURE, uuid: String = "xxx"): StubMapping =
    stubFor(post("/documents/$documentType/$uuid").willReturn(aResponse().withStatus(500)))

  fun stubGetDocumentByUuid(uuid: String = "xxx"): StubMapping =
    stubFor(
      get("/documents/$uuid/file")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "image/jpeg")
            .withBody(Files.readAllBytes(Paths.get("src/test/kotlin/uk/gov/justice/digital/hmpps/prisonperson/integration/assets/profile.jpeg")))
            .withStatus(200),
        ),
    )

  fun stubGetDocumentByUuidException(uuid: String = "xxx"): StubMapping =
    stubFor(get("/documents/$uuid/file").willReturn(aResponse().withStatus(500)))
}

class DocumentServiceExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val documentService = DocumentServiceServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    documentService.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    documentService.resetRequests()
    documentService.stubGetAllPicturesForPrisoner()
    documentService.stubGetAllPicturesForPrisonerException()
  }

  override fun afterAll(context: ExtensionContext) {
    documentService.stop()
  }
}
