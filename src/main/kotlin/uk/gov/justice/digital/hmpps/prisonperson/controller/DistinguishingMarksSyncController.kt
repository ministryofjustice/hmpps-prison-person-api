package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.DistinguishingMarkSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkSyncResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

class DistinguishingMarksSyncController {
  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__DISTHINGUISHING_MARKS_SYNC__RW')")
  @Operation(
    summary = "SYNC endpoint to sync changes to distinguishing marks made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO SYNC CHANGES MADE IN NOMIS.</b> " +
      "There is a separate endpoint for normal editing of the distinguishing marks." +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC__RW`" +
      "<br/><br/>Edits can be made to distinguishing marks in NOMIS to both the current booking " +
      "and to old bookings. This sync API can handle both by accepting the `appliesFrom` and " +
      "`appliesTo` timestamps - `appliesFrom` should be equal to the booking start date and and " +
      "`appliesTo` should be equal to the booking end date. The `createdAt` date should always be " +
      "the point in time that the user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Returns prisoner's distinguishing mark",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun syncDistinguishingMark(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestPart
    @Parameter(
      description = "File part of the multipart request",
      required = false,
    )
    file: MultipartFile?,
    @RequestBody
    @Validated
    distinguishingMarkSyncRequest: DistinguishingMarkSyncRequest,
  ): DistinguishingMarkSyncResponse {
    return DistinguishingMarkSyncResponse()
  }
}