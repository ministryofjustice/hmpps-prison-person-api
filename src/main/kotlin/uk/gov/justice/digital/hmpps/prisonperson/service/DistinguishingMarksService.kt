package uk.gov.justice.digital.hmpps.prisonperson.service

import jakarta.transaction.Transactional
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.config.GenericNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.DistinguishingMarkRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.DistinguishingMarkUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.DistinguishingMark
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.DistinguishingMarkImageRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.DistinguishingMarksRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCode
import java.util.UUID

@Service
class DistinguishingMarksService(
  private val documentServiceClient: DocumentServiceClient,
  private val distinguishingMarksRepository: DistinguishingMarksRepository,
  private val distinguishingMarkImageRepository: DistinguishingMarkImageRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) {
  fun getDistinguishingMarksForPrisoner(prisonerNumber: String): List<DistinguishingMarkDto> =
    distinguishingMarksRepository.findAllByPrisonerNumber(prisonerNumber).map {
      it.toDto()
    }

  fun getDistinguishingMarkById(uuid: UUID): DistinguishingMarkDto {
    val distinguishingMark = distinguishingMarksRepository.findById(uuid).orElseThrow {
      GenericNotFoundException("Distinguishing mark not found")
    }
    return distinguishingMark.toDto()
  }

  @Transactional
  fun create(
    file: MultipartFile?,
    fileType: MediaType?,
    distinguishingMarkRequest: DistinguishingMarkRequest,
  ): DistinguishingMarkDto {
    val username = authenticationFacade.getUserOrSystemInContext()

    val distinguishingMark = DistinguishingMark(
      prisonerNumber = distinguishingMarkRequest.prisonerNumber,
      markType = toReferenceDataCode(referenceDataCodeRepository, distinguishingMarkRequest.markType)!!,
      bodyPart = toReferenceDataCode(referenceDataCodeRepository, distinguishingMarkRequest.bodyPart)!!,
      side = toReferenceDataCode(referenceDataCodeRepository, distinguishingMarkRequest.side),
      partOrientation = toReferenceDataCode(referenceDataCodeRepository, distinguishingMarkRequest.partOrientation),
      comment = distinguishingMarkRequest.comment,
      createdBy = username,
    )

    if (file?.isEmpty == false) {
      if (fileType == null) {
        throw IllegalArgumentException("File type must not be null")
      }

      val uploadedDocument = documentServiceClient.putDocument(
        document = file.bytes,
        filename = file.originalFilename,
        documentType = DocumentType.DISTINGUISHING_MARK_IMAGE,
        meta = mapOf("prisonerNumber" to distinguishingMarkRequest.prisonerNumber),
        fileType,
        documentRequestContext = DocumentRequestContext(
          serviceName = "hmpps-prison-person-api",
          username,
        ),
      )

      distinguishingMark.addNewImage(
        uploadedDocument.documentUuid,
        distinguishingMarkImageRepository,
      )
    }

    // TODO consider deleting the doc from document service if the db save fails
    return distinguishingMarksRepository.save(distinguishingMark).toDto()
  }

  @Transactional
  fun update(
    uuid: UUID,
    request: DistinguishingMarkUpdateRequest,
  ): DistinguishingMarkDto {
    val distinguishingMark = distinguishingMarksRepository.findById(uuid).orElseThrow().apply {
      request.markType.let<String> { markType = toReferenceDataCode(referenceDataCodeRepository, it)!! }
      request.bodyPart.let<String> { bodyPart = toReferenceDataCode(referenceDataCodeRepository, it)!! }
      request.side.apply(this::side) { toReferenceDataCode(referenceDataCodeRepository, it) }
      request.partOrientation.apply(this::partOrientation) { toReferenceDataCode(referenceDataCodeRepository, it) }
      request.comment.apply(this::comment)
    }

    return distinguishingMarksRepository.save(distinguishingMark).toDto()
  }

  fun updatePhoto(uuid: UUID, file: MultipartFile?, fileType: MediaType): DistinguishingMarkDto {
    val username = authenticationFacade.getUserOrSystemInContext()
    val distinguishingMark = distinguishingMarksRepository.findById(uuid)
      .orElseThrow { GenericNotFoundException("Distinguishing mark not found") }

    if (file?.isEmpty == false) {
      val uploadedDocument = documentServiceClient.putDocument(
        document = file.bytes,
        filename = file.originalFilename,
        documentType = DocumentType.DISTINGUISHING_MARK_IMAGE,
        meta = mapOf("prisonerNumber" to distinguishingMark.prisonerNumber),
        fileType,
        documentRequestContext = DocumentRequestContext(
          serviceName = "hmpps-prison-person-api",
          username,
        ),
      )

      distinguishingMark.addNewImage(
        uploadedDocument.documentUuid,
        distinguishingMarkImageRepository,
      )
    }

    return distinguishingMarksRepository.save(distinguishingMark).toDto()
  }
}
