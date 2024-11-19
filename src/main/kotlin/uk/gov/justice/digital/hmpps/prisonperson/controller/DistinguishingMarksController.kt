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
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.DistinguishingMarkRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.DistinguishingMarkUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkDto
import uk.gov.justice.digital.hmpps.prisonperson.service.DistinguishingMarksService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.UUID

@RestController
@Tag(name = "Distinguishing marks", description = "Distinguishing marks linked to a prisoner")
@RequestMapping("/distinguishing-marks")
class DistinguishingMarksController(private val distinguishingMarksService: DistinguishingMarksService) {
  @GetMapping("/prisoner/{prisonerNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    summary = "Get all distinguishing marks for a prisoner",
    description = "description = \"Returns a list of distinguishing marks for a prisoner." +
      "Images associated with the distinguishing marks must be downloaded separately" +
      "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns distinguishing marks for the prisoner",
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
  fun getDistinguishingMarksForPrisoner(
    @PathVariable
    @Parameter(
      description = "The prisoner number",
      example = "A1234AA",
      required = true,
    )
    prisonerNumber: String,
  ): List<DistinguishingMarkDto> = distinguishingMarksService.getDistinguishingMarksForPrisoner(prisonerNumber)

  @GetMapping("/mark/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    summary = "Get distinguishing mark by id",
    description = "description = \"Returns the distinguishing mark requested." +
      "Images associated with the distinguishing mark must be downloaded separately" +
      "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns distinguishing marks for the prisoner",
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
  fun getDistinguishingMarkById(
    @PathVariable
    @Parameter(
      description = "The uuid of the distinguishing mark",
      required = true,
    )
    uuid: UUID,
  ): DistinguishingMarkDto? = distinguishingMarksService.getDistinguishingMarkById(uuid)

  @PostMapping("/mark", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW')")
  @Operation(
    summary = "Post an distinguishing mark",
    description = "description = \"Stores a new distinguishing mark entry in the database. " +
      "Optionally stores an image file supplied on the file attribute of a multipart/form-date submission  \n" +
      "returns the distinguishing mark detail\"\n" +
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
  fun putDistinguishingMark(
    @RequestPart
    @Parameter(
      description = "File part of the multipart request",
      required = false,
    )
    file: MultipartFile?,
    @Parameter(
      description = "The distinguishing mark request",
      required = true,
    )
    distinguishingMarkRequest: DistinguishingMarkRequest,
  ): DistinguishingMarkDto = distinguishingMarksService.create(
    file,
    fileType = MediaType.parseMediaType(file?.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE),
    distinguishingMarkRequest,
  )

  @PatchMapping("/mark/{uuid}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW')")
  @Operation(
    summary = "Updates the distinguishing mark",
    description = "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns prisoner's distinguishing mark",
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
  fun updateDistinguishingMark(
    @Schema(description = "The UUID of the mark", example = "3946f7d9-25d0-449f-bf17-1ade41559391", required = true)
    @PathVariable
    uuid: UUID,
    @RequestBody
    @Valid
    distinguishingMarkUpdateRequest: DistinguishingMarkUpdateRequest,
  ): DistinguishingMarkDto = distinguishingMarksService.update(uuid, distinguishingMarkUpdateRequest)

  @PostMapping("/mark/{uuid}/photo", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RW')")
  @Operation(
    summary = "Add a new photo for the distinguishing mark",
    description = "Stores a new distinguishing mark photo and sets it to the current photo for the distinguishing mark." +
      "The image file supplied on the file attribute of a multipart/form-date submission.  \n" +
      "Returns the distinguishing mark detail\n" +
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
  fun updateDistinguishingMarkPhoto(
    @Schema(description = "The UUID of the mark", example = "3946f7d9-25d0-449f-bf17-1ade41559391", required = true)
    @PathVariable
    uuid: UUID,
    @RequestPart
    @Parameter(
      description = "File part of the multipart request",
      required = false,
    )
    file: MultipartFile?,
  ) = distinguishingMarksService.updatePhoto(
    uuid,
    file,
    fileType = MediaType.parseMediaType(file?.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE),
  )
}
