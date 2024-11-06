package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.jpa.domain.AbstractAuditable_.createdBy
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.config.GenericNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.IdentifyingMarkRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.IdentifyingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.IdentifyingMark
import uk.gov.justice.digital.hmpps.prisonperson.jpa.IdentifyingMarkImage
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.IdentifyingMarksRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID
@ExtendWith(MockitoExtension::class)
class IdentifyingMarksServiceTest {
  @Mock
  lateinit var documentServiceClient: DocumentServiceClient

  @Mock
  lateinit var identifyingMarksRepository: IdentifyingMarksRepository

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var authenticationFacade: AuthenticationFacade

  @InjectMocks
  lateinit var underTest: IdentifyingMarksService

  @AfterEach
  fun afterEach() {
    reset(documentServiceClient, identifyingMarksRepository, referenceDataCodeRepository, authenticationFacade)
  }

  @Nested
  inner class GetIdentifyingMarksForPrisoner {
    @Test
    fun `can convert to dto and return`() {
      whenever(identifyingMarksRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).thenReturn(listOf(MARK1, MARK2))

      val result = underTest.getIdentifyingMarksForPrisoner(PRISONER_NUMBER)

      assertThat(result).isEqualTo(listOf(MARK_1_DTO, MARK_2_DTO))
    }

    @Test
    fun `can return empty list when none found`() {
      whenever(identifyingMarksRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).thenReturn(emptyList())

      val result = underTest.getIdentifyingMarksForPrisoner(PRISONER_NUMBER)

      assertThat(result).isEqualTo(emptyList<IdentifyingMarkDto>())
    }
  }

  @Nested
  inner class GetIdentifyingMarkById {
    @Test
    fun `can convert to dto and return`() {
      whenever(identifyingMarksRepository.findById(UUID.fromString(MARK1_ID))).thenReturn(Optional.of(MARK1))

      val result = underTest.getIdentifyingMarkById(MARK1_ID)

      assertThat(result).isEqualTo(MARK_1_DTO)
    }

    @Test
    fun `throws 404 when identifying mark not found`() {
      whenever(identifyingMarksRepository.findById(UUID.fromString(MARK1_ID))).thenReturn(Optional.empty())

      val exception = assertThrows(GenericNotFoundException::class.java) {
        underTest.getIdentifyingMarkById(MARK1_ID)
      }

      assertThat(exception.message).isEqualTo("Identifying mark not found")
    }
  }

  @Nested
  inner class Create {
    @Captor
    lateinit var identifyingMarkCaptor: ArgumentCaptor<IdentifyingMark>

    @Test
    fun `can create identifying mark with no file`() {
      whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
      whenever(referenceDataCodeRepository.findById("BODY_PART_LEG")).thenReturn(Optional.of(LEG_REFERENCE))
      whenever(referenceDataCodeRepository.findById("MARK_TYPE_SCAR")).thenReturn(Optional.of(SCAR_REFERENCE))
      whenever(referenceDataCodeRepository.findById("SIDE_L")).thenReturn(Optional.of(LEFT_SIDE_REFERENCE))
      whenever(referenceDataCodeRepository.findById("PART_ORIENT_CENTR")).thenReturn(Optional.of(CENTRE_REFERENCE))

      val identifyingMarkRequest = IdentifyingMarkRequest(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = "BODY_PART_LEG",
        markType = "MARK_TYPE_SCAR",
        side = "SIDE_L",
        partOrientation = "PART_ORIENT_CENTR",
        comment = "Comment",
      )

      val identifyingMark = IdentifyingMark(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = LEG_REFERENCE,
        markType = SCAR_REFERENCE,
        side = LEFT_SIDE_REFERENCE,
        partOrientation = CENTRE_REFERENCE,
        comment = "Comment",
        createdBy = USER1,
      )

      whenever(identifyingMarksRepository.save(any(IdentifyingMark::class.java))).thenReturn(MARK1)

      val result = underTest.create(null, MediaType.IMAGE_JPEG, identifyingMarkRequest)

      verify(identifyingMarksRepository).save(identifyingMarkCaptor.capture())
      val capturedIdentifyingMark = identifyingMarkCaptor.value

      assertThat(capturedIdentifyingMark.prisonerNumber).isEqualTo(identifyingMark.prisonerNumber)
      assertThat(capturedIdentifyingMark.bodyPart).isEqualTo(identifyingMark.bodyPart)
      assertThat(capturedIdentifyingMark.markType).isEqualTo(identifyingMark.markType)
      assertThat(capturedIdentifyingMark.side).isEqualTo(identifyingMark.side)
      assertThat(capturedIdentifyingMark.partOrientation).isEqualTo(identifyingMark.partOrientation)
      assertThat(capturedIdentifyingMark.comment).isEqualTo(identifyingMark.comment)
      assertThat(capturedIdentifyingMark.createdBy).isEqualTo(identifyingMark.createdBy)

      assertThat(result).isEqualTo(MARK_1_DTO)
    }

    @Test
    fun `can create identifying mark with file`() {
      // create mock MultiPartFile
      val fileType = MediaType.IMAGE_JPEG
      val file = MockMultipartFile(
        "file",
        "fileName.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "mock content".toByteArray(),
      )

      val documentDto = DocumentDto(
        documentUuid = "c46d0ce9-e586-4fa6-ae76-52ea8c242260",
        documentType = DocumentType.DISTINGUISHING_MARK_IMAGE,
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

      whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
      whenever(referenceDataCodeRepository.findById("BODY_PART_LEG")).thenReturn(Optional.of(LEG_REFERENCE))
      whenever(referenceDataCodeRepository.findById("MARK_TYPE_SCAR")).thenReturn(Optional.of(SCAR_REFERENCE))
      whenever(referenceDataCodeRepository.findById("SIDE_L")).thenReturn(Optional.of(LEFT_SIDE_REFERENCE))
      whenever(referenceDataCodeRepository.findById("PART_ORIENT_CENTR")).thenReturn(Optional.of(CENTRE_REFERENCE))
      whenever(documentServiceClient.putDocument(file.bytes, "fileName.jpg", DocumentType.DISTINGUISHING_MARK_IMAGE, mapOf("prisonerNumber" to "A1234AA"), fileType, DOCUMENT_REQ_CONTEXT)).thenReturn(documentDto)

      val identifyingMarkRequest = IdentifyingMarkRequest(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = "BODY_PART_LEG",
        markType = "MARK_TYPE_SCAR",
        side = "SIDE_L",
        partOrientation = "PART_ORIENT_CENTR",
        comment = "Comment",
      )

      val identifyingMark = IdentifyingMark(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = LEG_REFERENCE,
        markType = SCAR_REFERENCE,
        side = LEFT_SIDE_REFERENCE,
        partOrientation = CENTRE_REFERENCE,
        comment = "Comment",
        createdBy = USER1,
      )

      whenever(identifyingMarksRepository.save(any(IdentifyingMark::class.java))).thenReturn(MARK1)

      val result = underTest.create(file, MediaType.IMAGE_JPEG, identifyingMarkRequest)

      verify(identifyingMarksRepository).save(identifyingMarkCaptor.capture())
      val capturedIdentifyingMark = identifyingMarkCaptor.value

      assertThat(capturedIdentifyingMark.prisonerNumber).isEqualTo(identifyingMark.prisonerNumber)
      assertThat(capturedIdentifyingMark.bodyPart).isEqualTo(identifyingMark.bodyPart)
      assertThat(capturedIdentifyingMark.markType).isEqualTo(identifyingMark.markType)
      assertThat(capturedIdentifyingMark.side).isEqualTo(identifyingMark.side)
      assertThat(capturedIdentifyingMark.partOrientation).isEqualTo(identifyingMark.partOrientation)
      assertThat(capturedIdentifyingMark.comment).isEqualTo(identifyingMark.comment)
      assertThat(capturedIdentifyingMark.createdBy).isEqualTo(identifyingMark.createdBy)
      assertThat(capturedIdentifyingMark.photographUuids)
        .usingRecursiveComparison()
        .isEqualTo(
          setOf(
            IdentifyingMarkImage(
              identifyingMarkImageId = UUID.fromString(MARK1_ID),
              identifyingMark = capturedIdentifyingMark,
            ),
          ),
        )

      assertThat(result).isEqualTo(MARK_1_DTO)
    }

    @Test
    fun `does not put data in db if document service call fails`() {
      // create mock MultiPartFile
      val fileType = MediaType.IMAGE_JPEG
      val file = MockMultipartFile(
        "file",
        "fileName.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "mock content".toByteArray(),
      )

      whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
      whenever(referenceDataCodeRepository.findById("BODY_PART_LEG")).thenReturn(Optional.of(LEG_REFERENCE))
      whenever(referenceDataCodeRepository.findById("MARK_TYPE_SCAR")).thenReturn(Optional.of(SCAR_REFERENCE))
      whenever(referenceDataCodeRepository.findById("SIDE_L")).thenReturn(Optional.of(LEFT_SIDE_REFERENCE))
      whenever(referenceDataCodeRepository.findById("PART_ORIENT_CENTR")).thenReturn(Optional.of(CENTRE_REFERENCE))
      whenever(documentServiceClient.putDocument(file.bytes, "fileName.jpg", DocumentType.DISTINGUISHING_MARK_IMAGE, mapOf("prisonerNumber" to "A1234AA"), fileType, DOCUMENT_REQ_CONTEXT)).thenThrow(RuntimeException("Put document request failed"))

      val identifyingMarkRequest = IdentifyingMarkRequest(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = "BODY_PART_LEG",
        markType = "MARK_TYPE_SCAR",
        side = "SIDE_L",
        partOrientation = "PART_ORIENT_CENTR",
        comment = "Comment",
      )

      assertThrows<RuntimeException> {
        underTest.create(file, MediaType.IMAGE_JPEG, identifyingMarkRequest)
      }
      verify(identifyingMarksRepository, never()).save(any(IdentifyingMark::class.java))
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

    const val USER1 = "USER1"

    val BODY_PART_DOMAIN = ReferenceDataDomain("BODY_PART", "Body part identifying mark is on", 0, ZonedDateTime.now(), createdBy = "OMS_OWNER")

    val LEG_REFERENCE = ReferenceDataCode("BODY_PART_LEG", "LEG", BODY_PART_DOMAIN, "Leg", 1, createdBy = "OMS_OWNER")
    val SCAR_REFERENCE = ReferenceDataCode("MARK_TYPE_SCAR", "SCAR", BODY_PART_DOMAIN, "Scar", 0, createdBy = "OMS_OWNER")
    val LEFT_SIDE_REFERENCE = ReferenceDataCode("SIDE_L", "L", BODY_PART_DOMAIN, "Left", 0, createdBy = "OMS_OWNER")
    val CENTRE_REFERENCE = ReferenceDataCode("PART_ORIENT_CENTR", "CENTR", BODY_PART_DOMAIN, "Centre", 0, createdBy = "OMS_OWNER")

    val MARK1_ID = "c46d0ce9-e586-4fa6-ae76-52ea8c242260"
    val MARK1 = IdentifyingMark(
      identifyingMarkId = UUID.fromString(MARK1_ID),
      prisonerNumber = PRISONER_NUMBER,
      bodyPart = LEG_REFERENCE,
      markType = SCAR_REFERENCE,
      side = LEFT_SIDE_REFERENCE,
      partOrientation = CENTRE_REFERENCE,
      comment = "Comment",
      photographUuids = emptySet(),
      createdAt = ZonedDateTime.parse("2024-01-02T09:10:11+00:00"),
      createdBy = "PERSON1",
    )

    val MARK_1_DTO = IdentifyingMarkDto(
      id = "c46d0ce9-e586-4fa6-ae76-52ea8c242260",
      prisonerNumber = PRISONER_NUMBER,
      bodyPart = LEG_REFERENCE.toSimpleDto(),
      markType = SCAR_REFERENCE.toSimpleDto(),
      side = LEFT_SIDE_REFERENCE.toSimpleDto(),
      partOrientation = CENTRE_REFERENCE.toSimpleDto(),
      comment = "Comment",
      photographUuids = emptyList(),
      createdAt = ZonedDateTime.parse("2024-01-02T09:10:11+00:00"),
      createdBy = "PERSON1",
    )

    val MARK2 = IdentifyingMark(
      identifyingMarkId = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242261"),
      prisonerNumber = PRISONER_NUMBER,
      bodyPart = LEG_REFERENCE,
      markType = SCAR_REFERENCE,
      side = LEFT_SIDE_REFERENCE,
      partOrientation = CENTRE_REFERENCE,
      comment = "Comment",
      photographUuids = emptySet(),
      createdAt = ZonedDateTime.parse("2024-01-02T09:10:11+00:00"),
      createdBy = "PERSON1",
    )

    val MARK_2_DTO = IdentifyingMarkDto(
      id = "c46d0ce9-e586-4fa6-ae76-52ea8c242261",
      prisonerNumber = PRISONER_NUMBER,
      bodyPart = LEG_REFERENCE.toSimpleDto(),
      markType = SCAR_REFERENCE.toSimpleDto(),
      side = LEFT_SIDE_REFERENCE.toSimpleDto(),
      partOrientation = CENTRE_REFERENCE.toSimpleDto(),
      comment = "Comment",
      photographUuids = emptyList(),
      createdAt = ZonedDateTime.parse("2024-01-02T09:10:11+00:00"),
      createdBy = "PERSON1",
    )

    val DOCUMENT_REQ_CONTEXT = DocumentRequestContext(
      serviceName = "hmpps-prison-person-api",
      activeCaseLoadId = "USER1",
      username = null,
    )
  }
}
