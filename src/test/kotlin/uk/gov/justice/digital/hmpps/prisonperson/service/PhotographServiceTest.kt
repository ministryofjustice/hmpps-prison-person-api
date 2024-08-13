package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchRequestDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchResponseDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderBy
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderByDirection

@ExtendWith(MockitoExtension::class)
class PhotographServiceTest {
  @Mock
  lateinit var documentServiceClient: DocumentServiceClient

  @InjectMocks
  lateinit var underTest: PhotographService

  @Test
  fun `photographs not found`() {
    whenever(documentServiceClient.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)).thenReturn(EMPTY_RESPONSE)

    val result = underTest.getPhotographsForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)

    assertThat(result).isEqualTo(EMPTY_RESPONSE.results)
  }

  @Test
  fun `photographs retrieved`() {
    whenever(documentServiceClient.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)).thenReturn(SUCCESSFUL_RESPONSE)

    val result = underTest.getPhotographsForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)

    assertThat(result).isEqualTo(SUCCESSFUL_RESPONSE.results)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

    val DOCUMENT_REQ_CONTEXT = DocumentRequestContext(
      serviceName = "hmpps-prisoner-profile",
      activeCaseLoadId = "MDI",
      username = "USERNAME",
    )

    val REQUEST_DTO = DocumentSearchRequestDto(
      documentType = DocumentType.PRISONER_PROFILE_PICTURE,
      metadata = mapOf("prisonerNumber" to "A1234AA"),
      page = 1,
      pageSize = 10,
      orderBy = OrderBy.FILENAME,
      orderByDirection = OrderByDirection.DESC,
    )

    val SUCCESSFUL_RESPONSE = DocumentSearchResponseDto(
      request = REQUEST_DTO,
      results = listOf(
        DocumentDto(
          documentUuid = "abc",
          documentName = "name",
          documentFileName = "fileName",
          fileName = "fileName",
          fileExtension = "jpg",
          fileSize = 80,
          fileHash = "hash",
          mimeType = "mime",
          metadata = mapOf("prisonerNumber" to "A1234AA"),
          createdTime = "2021-01-01T00:00:00",
          createdByServiceName = "service",
          createdByUsername = "user",
        ),
      ),
      totalResultsCount = 1,
    )

    val EMPTY_RESPONSE = DocumentSearchResponseDto(
      request = REQUEST_DTO,
      results = emptyList(),
      totalResultsCount = 0,
    )
  }
}
