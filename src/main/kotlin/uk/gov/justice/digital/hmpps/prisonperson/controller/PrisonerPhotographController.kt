package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentDto
import uk.gov.justice.digital.hmpps.prisonperson.service.PhotographService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Prison Person Photographs", description = "The photographs linked to a prisoner")
@RequestMapping("/photographs")
class PrisonerPhotographController(private val photographService: PhotographService) {
  @GetMapping("/{prisonerNumber}/all", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    summary = "Get all photographs for a prisoner",
    description = "description = \"Returns document properties and metadata associated with the document. The document file must be \n" +
      "downloaded separately using the GET /COMING_SOON.\"\n" +
      "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns Photograph Document Data",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getAllProfilePicturesForPrisoner(
    @PathVariable
    @Parameter(
      description = "The prisoner number",
      example = "A1234AA",
      required = true,
    )
    prisonerNumber: String,
  ): List<DocumentDto> = photographService.getProfilePicsForPrisoner(prisonerNumber)

  @PostMapping("/prisoner-profile", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    summary = "Post a profile picture for a prisoner",
    description = "description = \"Stores a profile picture supplied on the file attribute of a multipart/form-date submission  \n" +
      "returns the meta data for the stored document\"\n" +
      "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns uploaded photograph document data",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun putPrisonerProfilePicture(
    @RequestPart
    @Parameter(
      description = "The prisoner number",
      example = "A1234AA",
      required = true,
    )
    prisonerNumber: String,
    @RequestPart
    @Parameter(
      description = "File part of the multipart request",
      required = true,
    )
    file: MultipartFile,
  ): DocumentDto = photographService.postProfilePicToDocumentService(
    file,
    fileType = MediaType.parseMediaType(file.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE),
    prisonerNumber,
  )

  @GetMapping(value = ["/{uuid}/file"])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    summary = "Get a document file from documents service using document uuid.",
    description = "Get a document from documents service using document uuid. Requires ROLE_CVL_ADMIN.",

    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns uploaded photograph",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getDocumentFileByUUI(
    @PathVariable("uuid")
    @Parameter(
      name = "uuid",
      description = "The uuid of the photograph required",
      required = true,
    )
    uuid: String,
  ): ResponseEntity<ByteArray?> {
    val (body, contentType) = photographService.getPicByUuid(uuid)
    return ResponseEntity
      .ok()
      .header("Content-Type", contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)
      .body(body)
  }
}
