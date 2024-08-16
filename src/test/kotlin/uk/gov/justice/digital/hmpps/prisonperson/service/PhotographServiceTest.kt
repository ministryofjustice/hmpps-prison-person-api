package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchRequestDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentSearchResponseDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderBy
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.OrderByDirection
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade

@ExtendWith(MockitoExtension::class)
class PhotographServiceTest {
  @Mock
  lateinit var documentServiceClient: DocumentServiceClient

  @Mock
  lateinit var authenticationFacade: AuthenticationFacade

  @InjectMocks
  lateinit var underTest: PhotographService

  @BeforeEach
  fun beforeEach() {
    whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
  }

  @Nested
  inner class GetPhotosForPrisoner {
    @Test
    fun `photographs not found`() {
      whenever(documentServiceClient.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)).thenReturn(EMPTY_RESPONSE)

      val result = underTest.getProfilePicsForPrisoner(PRISONER_NUMBER)

      assertThat(result).isEqualTo(EMPTY_RESPONSE.results)
    }

    @Test
    fun `photographs retrieved`() {
      whenever(documentServiceClient.getPhotosForPrisoner(PRISONER_NUMBER, DOCUMENT_REQ_CONTEXT)).thenReturn(SUCCESSFUL_SEARCH_RESPONSE)

      val result = underTest.getProfilePicsForPrisoner(PRISONER_NUMBER)

      assertThat(result).isEqualTo(SUCCESSFUL_SEARCH_RESPONSE.results)
    }
  }

  @Nested
  inner class PostProfilePicToDocumentService {
    @Test
    fun `post profile pic to document service`() {
      // create mock MultiPartFile
      val file = MockMultipartFile(
        "file",
        "fileName.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "mock content".toByteArray(),
      )

      val fileType = MediaType.IMAGE_JPEG
      val documentDto = DocumentDto(
        documentUuid = "abc",
        documentType = DocumentType.PRISONER_PROFILE_PICTURE,
        documentFilename = "fileName.jpg",
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
      whenever(documentServiceClient.putDocument(file.bytes, "fileName.jpg", DocumentType.PRISONER_PROFILE_PICTURE, mapOf("prisonerNumber" to "A1234AA"), fileType, DOCUMENT_REQ_CONTEXT)).thenReturn(documentDto)

      val result = underTest.postProfilePicToDocumentService(file, fileType, PRISONER_NUMBER)

      assertThat(result).isEqualTo(documentDto)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

    const val USER1 = "USER1"

    val DOCUMENT_REQ_CONTEXT = DocumentRequestContext(
      serviceName = "hmpps-prison-person-api",
      username = USER1,
    )

    val REQUEST_DTO = DocumentSearchRequestDto(
      documentType = DocumentType.PRISONER_PROFILE_PICTURE,
      metadata = mapOf("prisonerNumber" to "A1234AA"),
      page = 1,
      pageSize = 10,
      orderBy = OrderBy.FILENAME,
      orderByDirection = OrderByDirection.DESC,
    )

    val SUCCESSFUL_SEARCH_RESPONSE = DocumentSearchResponseDto(
      request = REQUEST_DTO,
      results = listOf(
        DocumentDto(
          documentUuid = "abc",
          documentType = DocumentType.PRISONER_PROFILE_PICTURE,
          documentFilename = "fileName.jpg",
          filename = "fileName",
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
