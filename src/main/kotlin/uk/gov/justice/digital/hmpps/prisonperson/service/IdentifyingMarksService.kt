package uk.gov.justice.digital.hmpps.prisonperson.service

import jakarta.transaction.Transactional
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.DocumentServiceClient
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentType
import uk.gov.justice.digital.hmpps.prisonperson.config.GenericNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.IdentifyingMarkRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.IdentifyingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.IdentifyingMark
import uk.gov.justice.digital.hmpps.prisonperson.jpa.IdentifyingMarkImage
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.IdentifyingMarksRepository
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.prisonperson.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.prisonperson.utils.toReferenceDataCode
import java.util.UUID

@Service
class IdentifyingMarksService(
  private val documentServiceClient: DocumentServiceClient,
  private val identifyingMarksRepository: IdentifyingMarksRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) {
  fun getIdentifyingMarksForPrisoner(prisonerNumber: String): List<IdentifyingMarkDto> =
    identifyingMarksRepository.findAllByPrisonerNumber(prisonerNumber).map {
      it.toDto()
    }

  fun getIdentifyingMarkById(uuid: String): IdentifyingMarkDto {
    val uuidObj = UUID.fromString(uuid)
    val identifyingMark = identifyingMarksRepository.findById(uuidObj).orElseThrow {
      GenericNotFoundException("Identifying mark not found")
    }
    return identifyingMark.toDto()
  }

  @Transactional
  fun create(
    file: MultipartFile?,
    fileType: MediaType?,
    identifyingMarkRequest: IdentifyingMarkRequest,
  ): IdentifyingMarkDto {
    val username = authenticationFacade.getUserOrSystemInContext()

    val identifyingMark = IdentifyingMark(
      prisonerNumber = identifyingMarkRequest.prisonerNumber,
      markType = toReferenceDataCode(referenceDataCodeRepository, identifyingMarkRequest.markType)!!,
      bodyPart = toReferenceDataCode(referenceDataCodeRepository, identifyingMarkRequest.bodyPart)!!,
      side = toReferenceDataCode(referenceDataCodeRepository, identifyingMarkRequest.side),
      partOrientation = toReferenceDataCode(referenceDataCodeRepository, identifyingMarkRequest.partOrientation),
      comment = identifyingMarkRequest.comment,
      createdBy = username,
    )

    if (file?.isEmpty == false) {
      if (fileType == null) {
        throw IllegalArgumentException("File type must not be null")
      }

      val uploadedDocument = documentServiceClient.putDocument(
        document = file.bytes,
        filename = file.originalFilename,
        documentType = DocumentType.PHYSICAL_IDENTIFIER_PICTURE,
        meta = mapOf("prisonerNumber" to identifyingMarkRequest.prisonerNumber),
        fileType,
        documentRequestContext = DocumentRequestContext(
          serviceName = "hmpps-prison-person-api",
          username,
        ),
      )

      val identifyingMarkImage = IdentifyingMarkImage(
        identifyingMarkImageId = UUID.fromString(uploadedDocument.documentUuid),
        identifyingMark = identifyingMark,
      )
      identifyingMark.photographUuids = setOf(identifyingMarkImage)
    }

    // TODO consider deleting the doc from document service if the db save fails
    return identifyingMarksRepository.save(identifyingMark).toDto()
  }
}
