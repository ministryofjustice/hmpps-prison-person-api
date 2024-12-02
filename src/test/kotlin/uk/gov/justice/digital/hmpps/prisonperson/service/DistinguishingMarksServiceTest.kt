package uk.gov.justice.digital.hmpps.prisonperson.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.config.GenericNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.DistinguishingMarkRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.DistinguishingMarkUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkImageDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMark
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMarkHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMarkImage
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.DistinguishingMarkImageRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.DistinguishingMarksRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectHistory
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import java.time.Clock
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DistinguishingMarksServiceTest {
  @Mock
  lateinit var documentServiceClient: DocumentServiceClient

  @Mock
  lateinit var distinguishingMarksRepository: DistinguishingMarksRepository

  @Mock
  lateinit var distinguishingMarkImageRepository: DistinguishingMarkImageRepository

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var authenticationFacade: AuthenticationFacade

  @InjectMocks
  lateinit var underTest: DistinguishingMarksService

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  private val savedDistinguishingMark = argumentCaptor<DistinguishingMark>()

  @AfterEach
  fun afterEach() {
    reset(
      documentServiceClient,
      distinguishingMarksRepository,
      distinguishingMarkImageRepository,
      referenceDataCodeRepository,
      authenticationFacade,
    )
  }

  @BeforeEach
  fun beforeEach() {
    whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
    whenever(referenceDataCodeRepository.findById(LEG_REFERENCE.id)).thenReturn(Optional.of(LEG_REFERENCE))
    whenever(referenceDataCodeRepository.findById(ARM_REFERENCE.id)).thenReturn(Optional.of(ARM_REFERENCE))
    whenever(referenceDataCodeRepository.findById(SCAR_REFERENCE.id)).thenReturn(Optional.of(SCAR_REFERENCE))
    whenever(referenceDataCodeRepository.findById(TATTOO_REFERENCE.id)).thenReturn(Optional.of(TATTOO_REFERENCE))
    whenever(referenceDataCodeRepository.findById(LEFT_SIDE_REFERENCE.id)).thenReturn(Optional.of(LEFT_SIDE_REFERENCE))
    whenever(referenceDataCodeRepository.findById(CENTRE_REFERENCE.id)).thenReturn(Optional.of(CENTRE_REFERENCE))
  }

  @Nested
  inner class GetDistinguishingMarksForPrisoner {
    @Test
    fun `can convert to dto and return`() {
      whenever(distinguishingMarksRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).thenReturn(listOf(MARK1, MARK2))

      val result = underTest.getDistinguishingMarksForPrisoner(PRISONER_NUMBER)

      assertThat(result).isEqualTo(listOf(MARK_1_DTO, MARK_2_DTO))
    }

    @Test
    fun `can return empty list when none found`() {
      whenever(distinguishingMarksRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).thenReturn(emptyList())

      val result = underTest.getDistinguishingMarksForPrisoner(PRISONER_NUMBER)

      assertThat(result).isEqualTo(emptyList<DistinguishingMarkDto>())
    }
  }

  @Nested
  inner class GetDistinguishingMarkById {
    @Test
    fun `can convert to dto and return`() {
      whenever(distinguishingMarksRepository.findById(MARK_1_ID)).thenReturn(Optional.of(MARK1))

      val result = underTest.getDistinguishingMarkById(MARK_1_ID)

      assertThat(result).isEqualTo(MARK_1_DTO)
    }

    @Test
    fun `throws 404 when distinguishing mark not found`() {
      whenever(distinguishingMarksRepository.findById(MARK_1_ID)).thenReturn(Optional.empty())

      val exception = assertThrows(GenericNotFoundException::class.java) {
        underTest.getDistinguishingMarkById(MARK_1_ID)
      }

      assertThat(exception.message).isEqualTo("Distinguishing mark not found")
    }
  }

  @Nested
  inner class Create {
    @Captor
    lateinit var distinguishingMarkCaptor: ArgumentCaptor<DistinguishingMark>

    @Test
    fun `can create distinguishing mark with no file`() {
      val distinguishingMarkRequest = DistinguishingMarkRequest(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = "BODY_PART_LEG",
        markType = "MARK_TYPE_SCAR",
        side = "SIDE_L",
        partOrientation = "PART_ORIENT_CENTR",
        comment = "Comment",
      )

      val distinguishingMark = DistinguishingMark(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = LEG_REFERENCE,
        markType = SCAR_REFERENCE,
        side = LEFT_SIDE_REFERENCE,
        partOrientation = CENTRE_REFERENCE,
        comment = "Comment",
        createdBy = USER1,
      )

      whenever(distinguishingMarksRepository.save(any(DistinguishingMark::class.java))).thenReturn(MARK1)

      val result = underTest.create(null, MediaType.IMAGE_JPEG, distinguishingMarkRequest)

      verify(distinguishingMarksRepository).save(distinguishingMarkCaptor.capture())
      val capturedDistinguishingMark = distinguishingMarkCaptor.value

      assertThat(capturedDistinguishingMark.prisonerNumber).isEqualTo(distinguishingMark.prisonerNumber)
      assertThat(capturedDistinguishingMark.bodyPart).isEqualTo(distinguishingMark.bodyPart)
      assertThat(capturedDistinguishingMark.markType).isEqualTo(distinguishingMark.markType)
      assertThat(capturedDistinguishingMark.side).isEqualTo(distinguishingMark.side)
      assertThat(capturedDistinguishingMark.partOrientation).isEqualTo(distinguishingMark.partOrientation)
      assertThat(capturedDistinguishingMark.comment).isEqualTo(distinguishingMark.comment)
      assertThat(capturedDistinguishingMark.createdBy).isEqualTo(distinguishingMark.createdBy)
      capturedDistinguishingMark.expectHistory(
        HistoryComparison(
          value = capturedDistinguishingMark,
          createdAt = NOW,
          createdBy = USER1,
          appliesFrom = NOW,
          appliesTo = null,
        ),
      )

      assertThat(result).isEqualTo(MARK_1_DTO)
    }

    @Test
    fun `can create distinguishing mark with file`() {
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
      whenever(
        documentServiceClient.putDocument(
          file.bytes,
          "fileName.jpg",
          DocumentType.DISTINGUISHING_MARK_IMAGE,
          mapOf("prisonerNumber" to "A1234AA"),
          fileType,
          DOCUMENT_REQ_CONTEXT,
        ),
      ).thenReturn(documentDto)

      val distinguishingMarkRequest = DistinguishingMarkRequest(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = "BODY_PART_LEG",
        markType = "MARK_TYPE_SCAR",
        side = "SIDE_L",
        partOrientation = "PART_ORIENT_CENTR",
        comment = "Comment",
      )

      val distinguishingMark = DistinguishingMark(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = LEG_REFERENCE,
        markType = SCAR_REFERENCE,
        side = LEFT_SIDE_REFERENCE,
        partOrientation = CENTRE_REFERENCE,
        comment = "Comment",
        createdBy = USER1,
      )

      whenever(distinguishingMarksRepository.save(any(DistinguishingMark::class.java))).thenReturn(MARK1)

      val result = underTest.create(file, MediaType.IMAGE_JPEG, distinguishingMarkRequest)

      verify(distinguishingMarksRepository).save(distinguishingMarkCaptor.capture())
      val capturedDistinguishingMark = distinguishingMarkCaptor.value

      assertThat(capturedDistinguishingMark.prisonerNumber).isEqualTo(distinguishingMark.prisonerNumber)
      assertThat(capturedDistinguishingMark.bodyPart).isEqualTo(distinguishingMark.bodyPart)
      assertThat(capturedDistinguishingMark.markType).isEqualTo(distinguishingMark.markType)
      assertThat(capturedDistinguishingMark.side).isEqualTo(distinguishingMark.side)
      assertThat(capturedDistinguishingMark.partOrientation).isEqualTo(distinguishingMark.partOrientation)
      assertThat(capturedDistinguishingMark.comment).isEqualTo(distinguishingMark.comment)
      assertThat(capturedDistinguishingMark.createdBy).isEqualTo(distinguishingMark.createdBy)
      assertThat(capturedDistinguishingMark.photographUuids).usingRecursiveComparison().isEqualTo(
        setOf(
          DistinguishingMarkImage(
            distinguishingMarkImageId = MARK_1_ID,
            distinguishingMark = capturedDistinguishingMark,
            latest = true,
          ),
        ),
      )

      capturedDistinguishingMark.expectHistory(
        HistoryComparison(
          value = capturedDistinguishingMark,
          createdAt = NOW,
          createdBy = USER1,
          appliesFrom = NOW,
          appliesTo = null,
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
      whenever(
        documentServiceClient.putDocument(
          file.bytes,
          "fileName.jpg",
          DocumentType.DISTINGUISHING_MARK_IMAGE,
          mapOf("prisonerNumber" to "A1234AA"),
          fileType,
          DOCUMENT_REQ_CONTEXT,
        ),
      ).thenThrow(RuntimeException("Put document request failed"))

      val distinguishingMarkRequest = DistinguishingMarkRequest(
        prisonerNumber = PRISONER_NUMBER,
        bodyPart = "BODY_PART_LEG",
        markType = "MARK_TYPE_SCAR",
        side = "SIDE_L",
        partOrientation = "PART_ORIENT_CENTR",
        comment = "Comment",
      )

      assertThrows<RuntimeException> {
        underTest.create(file, MediaType.IMAGE_JPEG, distinguishingMarkRequest)
      }
      verify(distinguishingMarksRepository, never()).save(any(DistinguishingMark::class.java))
    }
  }

  @Nested
  inner class Update {
    @Captor
    lateinit var updatedDistinguishingMark: ArgumentCaptor<DistinguishingMark>

    @BeforeEach
    fun beforeEach() {
      whenever(distinguishingMarksRepository.save(updatedDistinguishingMark.capture())).thenAnswer { updatedDistinguishingMark.firstValue }
    }

    @Test
    fun `updating existing distinguishing mark`() {
      whenever(distinguishingMarksRepository.findById(MARK_1_ID)).thenReturn(
        Optional.of(
          DistinguishingMark(
            prisonerNumber = PRISONER_NUMBER,
            distinguishingMarkId = MARK_1_ID,
            createdAt = NOW.minusDays(1),
            createdBy = USER1,
            bodyPart = LEG_REFERENCE,
            markType = SCAR_REFERENCE,
          ).also {
            it.history.add(
              DistinguishingMarkHistory(
                mark = DistinguishingMark(
                  prisonerNumber = PRISONER_NUMBER,
                  distinguishingMarkId = MARK_1_ID,
                  createdAt = THEN,
                  createdBy = USER1,
                  bodyPart = LEG_REFERENCE,
                  markType = SCAR_REFERENCE,
                ),
                valueJson = DistinguishingMark(
                  prisonerNumber = PRISONER_NUMBER,
                  distinguishingMarkId = MARK_1_ID,
                  createdAt = THEN,
                  createdBy = USER1,
                  bodyPart = LEG_REFERENCE,
                  markType = SCAR_REFERENCE,
                ).toHistoryDto(),
                createdAt = THEN,
                createdBy = USER1,
                anomalous = false,
                appliesFrom = THEN,
                source = Source.DPS,
              ),
            )
          },
        ),
      )

      val attributes = mutableMapOf<String, Any?>(
        Pair("bodyPart", ARM_REFERENCE.id),
        Pair("markType", TATTOO_REFERENCE.id),
        Pair("side", LEFT_SIDE_REFERENCE.id),
        Pair("partOrientation", CENTRE_REFERENCE.id),
        Pair("comment", MARK1.comment),
      )

      val result = underTest.update(MARK_1_ID, DistinguishingMarkUpdateRequest(attributes))
      assertThat(result).isEqualTo(
        DistinguishingMarkDto(
          id = MARK_1_ID.toString(),
          prisonerNumber = PRISONER_NUMBER,
          bodyPart = ARM_REFERENCE.toSimpleDto(),
          markType = TATTOO_REFERENCE.toSimpleDto(),
          side = LEFT_SIDE_REFERENCE.toSimpleDto(),
          partOrientation = CENTRE_REFERENCE.toSimpleDto(),
          comment = MARK1.comment,
          createdAt = NOW.minusDays(1),
          createdBy = USER1,
        ),
      )

      with(updatedDistinguishingMark.firstValue) {
        assertThat(bodyPart).isEqualTo(ARM_REFERENCE)
        assertThat(markType).isEqualTo(TATTOO_REFERENCE)
        assertThat(side).isEqualTo(LEFT_SIDE_REFERENCE)
        assertThat(partOrientation).isEqualTo(CENTRE_REFERENCE)
        assertThat(comment).isEqualTo(MARK1.comment)

        expectHistory(
          HistoryComparison(
            value = DistinguishingMark(
              prisonerNumber = PRISONER_NUMBER,
              distinguishingMarkId = MARK_1_ID,
              createdAt = THEN,
              createdBy = USER1,
              bodyPart = LEG_REFERENCE,
              markType = SCAR_REFERENCE,
            ),
            createdAt = THEN,
            createdBy = USER1,
            anomalous = false,
            appliesFrom = THEN,
            appliesTo = NOW,
          ),

          HistoryComparison(
            value = DistinguishingMark(
              prisonerNumber = PRISONER_NUMBER,
              distinguishingMarkId = MARK_1_ID,
              createdAt = THEN,
              createdBy = USER1,
              bodyPart = ARM_REFERENCE,
              markType = TATTOO_REFERENCE,
              side = LEFT_SIDE_REFERENCE,
              partOrientation = CENTRE_REFERENCE,
              comment = MARK1.comment,
            ),
            createdAt = NOW,
            createdBy = USER1,
            anomalous = false,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )
      }
    }

    @Test
    fun `updating a distinguishing mark with an initial image`() {
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

      whenever(
        documentServiceClient.putDocument(
          file.bytes,
          "fileName.jpg",
          DocumentType.DISTINGUISHING_MARK_IMAGE,
          mapOf("prisonerNumber" to "A1234AA"),
          fileType,
          DOCUMENT_REQ_CONTEXT,
        ),
      ).thenReturn(documentDto)

      whenever(distinguishingMarksRepository.findById(MARK_1_ID)).thenReturn(
        Optional.of(
          DistinguishingMark(
            prisonerNumber = PRISONER_NUMBER,
            distinguishingMarkId = MARK_1_ID,
            createdAt = THEN,
            createdBy = USER1,
            bodyPart = LEG_REFERENCE,
            markType = SCAR_REFERENCE,
            photographUuids = mutableListOf(),
          ).also {
            it.history.add(
              DistinguishingMarkHistory(
                mark = DistinguishingMark(
                  prisonerNumber = PRISONER_NUMBER,
                  distinguishingMarkId = MARK_1_ID,
                  createdAt = THEN,
                  createdBy = USER1,
                  bodyPart = LEG_REFERENCE,
                  markType = SCAR_REFERENCE,
                ),
                valueJson = DistinguishingMark(
                  prisonerNumber = PRISONER_NUMBER,
                  distinguishingMarkId = MARK_1_ID,
                  createdAt = THEN,
                  createdBy = USER1,
                  bodyPart = LEG_REFERENCE,
                  markType = SCAR_REFERENCE,
                ).toHistoryDto(),
                createdAt = THEN,
                createdBy = USER1,
                anomalous = false,
                appliesFrom = THEN,
                source = Source.DPS,
              ),
            )
          },
        ),
      )

      val result = underTest.updatePhoto(MARK_1_ID, file, fileType)
      assertThat(result.photographUuids).isEqualTo(
        listOf(
          DistinguishingMarkImageDto("c46d0ce9-e586-4fa6-ae76-52ea8c242260", true),
        ),
      )

      with(updatedDistinguishingMark.firstValue) {
        assertThat(bodyPart).isEqualTo(LEG_REFERENCE)
        assertThat(markType).isEqualTo(SCAR_REFERENCE)

        expectHistory(
          HistoryComparison(
            value = DistinguishingMark(
              prisonerNumber = PRISONER_NUMBER,
              distinguishingMarkId = MARK_1_ID,
              createdAt = THEN,
              createdBy = USER1,
              bodyPart = LEG_REFERENCE,
              markType = SCAR_REFERENCE,
              photographUuids = mutableListOf(),
            ),
            createdAt = THEN,
            createdBy = USER1,
            anomalous = false,
            appliesFrom = THEN,
            appliesTo = NOW,
          ),

          HistoryComparison(
            value = DistinguishingMark(
              prisonerNumber = PRISONER_NUMBER,
              distinguishingMarkId = MARK_1_ID,
              createdAt = THEN,
              createdBy = USER1,
              bodyPart = LEG_REFERENCE,
              markType = SCAR_REFERENCE,
            ).also {
              it.photographUuids = mutableListOf(
                DistinguishingMarkImage(
                  distinguishingMarkImageId = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242260"),
                  distinguishingMark = it,
                  latest = true,
                ),
              )
            },
            createdAt = NOW,
            createdBy = USER1,
            anomalous = false,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )
      }
    }

    @Test
    fun `updating distinguishing marks image`() {
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

      whenever(
        documentServiceClient.putDocument(
          file.bytes,
          "fileName.jpg",
          DocumentType.DISTINGUISHING_MARK_IMAGE,
          mapOf("prisonerNumber" to "A1234AA"),
          fileType,
          DOCUMENT_REQ_CONTEXT,
        ),
      ).thenReturn(documentDto)

      whenever(distinguishingMarksRepository.findById(MARK_1_ID)).thenReturn(
        Optional.of(
          DistinguishingMark(
            prisonerNumber = PRISONER_NUMBER,
            distinguishingMarkId = MARK_1_ID,
            createdAt = THEN,
            createdBy = USER1,
            bodyPart = LEG_REFERENCE,
            markType = SCAR_REFERENCE,
          ).also {
            it.history.add(
              DistinguishingMarkHistory(
                mark = DistinguishingMark(
                  prisonerNumber = PRISONER_NUMBER,
                  distinguishingMarkId = MARK_1_ID,
                  createdAt = THEN,
                  createdBy = USER1,
                  bodyPart = LEG_REFERENCE,
                  markType = SCAR_REFERENCE,
                ).apply {
                  photographUuids = mutableListOf(
                    DistinguishingMarkImage(
                      distinguishingMarkImageId = UUID.fromString("21855879-1fce-4493-b1eb-0345563eb607"),
                      distinguishingMark = this,
                      latest = true,
                    ),
                  )
                },
                valueJson = DistinguishingMark(
                  prisonerNumber = PRISONER_NUMBER,
                  distinguishingMarkId = MARK_1_ID,
                  createdAt = THEN,
                  createdBy = USER1,
                  bodyPart = LEG_REFERENCE,
                  markType = SCAR_REFERENCE,
                ).apply {
                  photographUuids = mutableListOf(
                    DistinguishingMarkImage(
                      distinguishingMarkImageId = UUID.fromString("21855879-1fce-4493-b1eb-0345563eb607"),
                      distinguishingMark = this,
                      latest = true,
                    ),
                  )
                }.toHistoryDto(),
                createdAt = THEN,
                createdBy = USER1,
                anomalous = false,
                appliesFrom = THEN,
                source = Source.DPS,
              ),
            )
          }.apply {
            photographUuids = mutableListOf(
              DistinguishingMarkImage(
                distinguishingMarkImageId = UUID.fromString("21855879-1fce-4493-b1eb-0345563eb607"),
                distinguishingMark = this,
                latest = true,
              ),
            )
          },
        ),
      )

      val result = underTest.updatePhoto(MARK_1_ID, file, fileType)
      assertThat(result.photographUuids).isEqualTo(
        listOf(
          DistinguishingMarkImageDto("21855879-1fce-4493-b1eb-0345563eb607", false),
          DistinguishingMarkImageDto("c46d0ce9-e586-4fa6-ae76-52ea8c242260", true),
        ),
      )

      with(updatedDistinguishingMark.firstValue) {
        assertThat(bodyPart).isEqualTo(LEG_REFERENCE)
        assertThat(markType).isEqualTo(SCAR_REFERENCE)

        expectHistory(
          HistoryComparison(
            value = DistinguishingMark(
              prisonerNumber = PRISONER_NUMBER,
              distinguishingMarkId = MARK_1_ID,
              createdAt = THEN,
              createdBy = USER1,
              bodyPart = LEG_REFERENCE,
              markType = SCAR_REFERENCE,
            ).also {
              it.photographUuids = mutableListOf(
                DistinguishingMarkImage(
                  distinguishingMarkImageId = UUID.fromString("21855879-1fce-4493-b1eb-0345563eb607"),
                  distinguishingMark = this,
                  latest = true,
                ),
              )
            },
            createdAt = THEN,
            createdBy = USER1,
            anomalous = false,
            appliesFrom = THEN,
            appliesTo = NOW,
          ),

          HistoryComparison(
            value = DistinguishingMark(
              prisonerNumber = PRISONER_NUMBER,
              distinguishingMarkId = MARK_1_ID,
              createdAt = THEN,
              createdBy = USER1,
              bodyPart = LEG_REFERENCE,
              markType = SCAR_REFERENCE,
            ).also {
              it.photographUuids = mutableListOf(
                DistinguishingMarkImage(
                  distinguishingMarkImageId = UUID.fromString("21855879-1fce-4493-b1eb-0345563eb607"),
                  distinguishingMark = this,
                  latest = false,
                ),

                DistinguishingMarkImage(
                  distinguishingMarkImageId = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242260"),
                  distinguishingMark = it,
                  latest = true,
                ),
              )
            },
            createdAt = NOW,
            createdBy = USER1,
            anomalous = false,
            appliesFrom = NOW,
            appliesTo = null,
          ),
        )
      }
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    val NOW: ZonedDateTime = ZonedDateTime.now()
    val THEN: ZonedDateTime = NOW.minusDays(1)

    const val USER1 = "USER1"

    val BODY_PART_DOMAIN = ReferenceDataDomain(
      "BODY_PART",
      "Body part distinguishing mark is on",
      0,
      ZonedDateTime.now(),
      createdBy = "OMS_OWNER",
    )

    val LEG_REFERENCE = ReferenceDataCode("BODY_PART_LEG", "LEG", BODY_PART_DOMAIN, "Leg", 1, createdBy = "OMS_OWNER")
    val ARM_REFERENCE = ReferenceDataCode("BODY_PART_ARM", "ARM", BODY_PART_DOMAIN, "Arm", 1, createdBy = "OMS_OWNER")
    val SCAR_REFERENCE =
      ReferenceDataCode("MARK_TYPE_SCAR", "SCAR", BODY_PART_DOMAIN, "Scar", 0, createdBy = "OMS_OWNER")
    val TATTOO_REFERENCE =
      ReferenceDataCode("MARK_TYPE_TATTOO", "TATTOO", BODY_PART_DOMAIN, "Tattoo", 0, createdBy = "OMS_OWNER")
    val LEFT_SIDE_REFERENCE = ReferenceDataCode("SIDE_L", "L", BODY_PART_DOMAIN, "Left", 0, createdBy = "OMS_OWNER")
    val CENTRE_REFERENCE =
      ReferenceDataCode("PART_ORIENT_CENTR", "CENTR", BODY_PART_DOMAIN, "Centre", 0, createdBy = "OMS_OWNER")

    val MARK_1_ID = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242260")
    val MARK1 = DistinguishingMark(
      distinguishingMarkId = MARK_1_ID,
      prisonerNumber = PRISONER_NUMBER,
      bodyPart = LEG_REFERENCE,
      markType = SCAR_REFERENCE,
      side = LEFT_SIDE_REFERENCE,
      partOrientation = CENTRE_REFERENCE,
      comment = "Comment",
      photographUuids = mutableListOf(),
      createdAt = ZonedDateTime.parse("2024-01-02T09:10:11+00:00"),
      createdBy = "PERSON1",
    )

    val MARK_1_DTO = DistinguishingMarkDto(
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

    val MARK2 = DistinguishingMark(
      distinguishingMarkId = UUID.fromString("c46d0ce9-e586-4fa6-ae76-52ea8c242261"),
      prisonerNumber = PRISONER_NUMBER,
      bodyPart = LEG_REFERENCE,
      markType = SCAR_REFERENCE,
      side = LEFT_SIDE_REFERENCE,
      partOrientation = CENTRE_REFERENCE,
      comment = "Comment",
      photographUuids = mutableListOf(),
      createdAt = ZonedDateTime.parse("2024-01-02T09:10:11+00:00"),
      createdBy = "PERSON1",
    )

    val MARK_2_DTO = DistinguishingMarkDto(
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
