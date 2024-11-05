package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.IdentifyingMarkRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.IdentifyingMarkUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.IdentifyingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.service.IdentifyingMarksService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Identifying marks", description = "Identifying marks linked to a prisoner")
@RequestMapping("/identifying-marks")
class IdentifyingMarksController(private val identifyingMarksService: IdentifyingMarksService) {
  @GetMapping("/prisoner/{prisonerNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    summary = "Get all identifying marks for a prisoner",
    description = "description = \"Returns a list of identifying marks for a prisoner." +
      "Images associated with the identifying marks must be downloaded separately" +
      "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns identifying marks for the prisoner",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getIdentifyingMarksForPrisoner(
    @PathVariable
    @Parameter(
      description = "The prisoner number",
      example = "A1234AA",
      required = true,
    )
    prisonerNumber: String,
  ): List<IdentifyingMarkDto> = identifyingMarksService.getIdentifyingMarksForPrisoner(prisonerNumber)

  @GetMapping("/mark/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    summary = "Get identifying mark by id",
    description = "description = \"Returns the identifying mark requested." +
      "Images associated with the identifying mark must be downloaded separately" +
      "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns identifying marks for the prisoner",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getIdentifyingMarkById(
    @PathVariable
    @Parameter(
      description = "The uuid of the identifying mark",
      required = true,
    )
    uuid: String,
  ): IdentifyingMarkDto? = identifyingMarksService.getIdentifyingMarkById(uuid)

  @PostMapping("/mark", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW')")
  @Operation(
    summary = "Post an identifying mark",
    description = "description = \"Stores a new identifying mark entry in the database. " +
      "Optionally stores an image file supplied on the file attribute of a multipart/form-date submission  \n" +
      "returns the identifying mark detail\"\n" +
      "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW`",
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
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun putIdentifyingMark(
    @RequestPart
    @Parameter(
      description = "File part of the multipart request",
      required = false,
    )
    file: MultipartFile?,
    @Parameter(
      description = "The identifying mark request",
      required = true,
    )
    identifyingMarkRequest: IdentifyingMarkRequest,
  ): IdentifyingMarkDto = identifyingMarksService.create(
    file,
    fileType = MediaType.parseMediaType(file?.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE),
    identifyingMarkRequest,
  )

  @PatchMapping("/mark/{uuid}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW')")
  @Operation(
    summary = "Updates the identifying mark",
    description = "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Returns prisoner's identifying mark",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun updateIdentifyingMark(
    @Schema(description = "The UUID of the mark", example = "A1234AA", required = true)
    @PathVariable
    uuid: String,
    @RequestBody
    @Valid
    identifyingMarkUpdateRequest: IdentifyingMarkUpdateRequest,
  ): IdentifyingMarkDto = identifyingMarksService.update(uuid, identifyingMarkUpdateRequest)
}
