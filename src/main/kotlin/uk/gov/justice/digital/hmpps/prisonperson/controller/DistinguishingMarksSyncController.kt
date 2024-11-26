package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.distinguishingmarks.DistinguishingMarkCreationSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.distinguishingmarks.DistinguishingMarkImageUpdateSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.distinguishingmarks.DistinguishingMarkUpdateSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkImageSyncResponse
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.DistinguishingMarkSyncResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

/*
multiple end points
Create / Update / Delete distinguishing mark
Create / Update(latest/not latest) / Delete image
 */
@RestController
@Tag(name = "Distinguishing marks")
@Tag(
  name = "Sync with NOMIS",
  description = "Endpoints to keep the Prison Person database in sync with changes in the NOMIS database",
)
@RequestMapping("/sync/prisoners/{prisonerNumber}/distinguishing-marks", produces = [MediaType.APPLICATION_JSON_VALUE])
class DistinguishingMarksSyncController {
  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__DISTHINGUISHING_MARKS_SYNC__RW')")
  @Operation(
    summary = "SYNC endpoint to sync new distinguishing marks made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO SYNC CHANGES MADE IN NOMIS.</b> " +
      "There is a separate endpoint for normal creation of the distinguishing marks." +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC__RW`" +
      "<br/><br/>Distinguishing marks can be added to both current and prior bookings. " +
      "This sync API can handle both by accepting the `appliesFrom` and " +
      "`appliesTo` timestamps - `appliesFrom` should be equal to the booking start date and and " +
      "`appliesTo` should be equal to the booking end date. The `createdAt` date should always be " +
      "the point in time that the user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Returns ID of created distinguishing mark",
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
  fun createNewDistinguishingMark(
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Validated
    distinguishingMarkSyncRequest: DistinguishingMarkCreationSyncRequest,
  ): DistinguishingMarkSyncResponse {
    // Create new distinguishing mark
    // Needs to accept applies from/to and someone could create a new mark on a historical entry
    // Creation is a separate event from creating images
    return DistinguishingMarkSyncResponse(UUID.fromString("3b47dc4c-5d61-434e-ae7c-29e10718e780"))
  }

  @PutMapping("/{distinguishingMarkId}")
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
        responseCode = "200",
        description = "Returns ID of the edited distinguishing mark",
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
  fun syncUpdateDistinguishingMark(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @Schema(
      description = "The UUID of the distinguishing mark to delete",
      example = "22198ef9-445d-449a-b016-0521ebfb5c2d",
      required = true,
    )
    @PathVariable
    distinguishingMarkId: UUID,
    @RequestBody
    @Validated
    distinguishingMarkSyncRequest: DistinguishingMarkUpdateSyncRequest,
  ): DistinguishingMarkSyncResponse {
    // Standard update endpoint
    // Update values, update history for distinguishing mark (historical edits happen)
    // Does NOT handle images, those are handled below
    return DistinguishingMarkSyncResponse(UUID.fromString("3b47dc4c-5d61-434e-ae7c-29e10718e780"))
  }

  @DeleteMapping("/{distinguishingMarkId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC_RW')")
  @Operation(
    summary = "SYNC endpoint to sync distinguishing mark deletions made in NOMIS",
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
        responseCode = "200",
        description = "Returns ID of the deleted distinguishing mark",
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
  fun syncDeleteDistinguishingMark(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @Schema(
      description = "The UUID of the distinguishing mark to delete",
      example = "22198ef9-445d-449a-b016-0521ebfb5c2d",
      required = true,
    )
    @PathVariable
    distinguishingMarkId: UUID,
  ): DistinguishingMarkSyncResponse {
    // Validate distinguishing mark belongs to the prisoner number
    // Delete distinguishing mark (set 'valid to' in history to the moment it was deleted?)
    // Return deleted ID
    return DistinguishingMarkSyncResponse(UUID.fromString("3b47dc4c-5d61-434e-ae7c-29e10718e780"))
  }

  @PostMapping("/{distinguishingMarkId}/images")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC_RW')")
  @Operation(
    summary = "SYNC endpoint to sync distinguishing mark image creations made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO SYNC CHANGES MADE IN NOMIS.</b> " +
      "There is a separate endpoint for normal creation of the distinguishing mark images." +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC__RW`" +
      "<br/><br/>Images can be added to distinguishing marks in NOMIS to both the current booking " +
      "and to old bookings. This sync API can handle both by accepting the `appliesFrom` and " +
      "`appliesTo` timestamps - `appliesFrom` should be equal to the booking start date and and " +
      "`appliesTo` should be equal to the booking end date. The `createdAt` date should always be " +
      "the point in time that the user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns UUID of created distinguishing mark image",
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
  fun syncCreateDistinguishingMarkImage(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @Schema(
      description = "The UUID of the distinguishing mark to add the image to",
      example = "22198ef9-445d-449a-b016-0521ebfb5c2d",
      required = true,
    )
    @PathVariable
    distinguishingMarkId: UUID,
    @RequestPart
    @Parameter(
      description = "File part of the multipart request",
      required = false,
    )
    file: MultipartFile?,
  ): DistinguishingMarkImageSyncResponse {
    // Needs applies from/to for historical images.
    // Historical images will need to be added to the array in the correct entry in the history table
    return DistinguishingMarkImageSyncResponse(UUID.fromString("3b47dc4c-5d61-434e-ae7c-29e10718e780"))
  }

  @PutMapping("/{distinguishingMarkId}/images/{imageId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC_RW')")
  @Operation(
    summary = "SYNC endpoint to sync distinguishing mark image edits made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO SYNC CHANGES MADE IN NOMIS.</b> " +
      "There is a separate endpoint for normal editing of the distinguishing mark images." +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC__RW`" +
      "<br/><br/>Distinguishing mark images can be edited in NOMIS on both the current booking " +
      "and to old bookings. This sync API can handle both by accepting the `appliesFrom` and " +
      "`appliesTo` timestamps - `appliesFrom` should be equal to the booking start date and and " +
      "`appliesTo` should be equal to the booking end date. The `createdAt` date should always be " +
      "the point in time that the user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns UUID of updated distinguishing mark image",
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
  fun syncUpdateDistinguishingMarkImage(
    @PathVariable
    prisonerNumber: String,
    @PathVariable
    distinguishingMarkId: String,
    @PathVariable
    imageId: String,
    @RequestBody
    @Validated
    request: DistinguishingMarkImageUpdateSyncRequest,
  ): DistinguishingMarkImageSyncResponse {
    // Only relates to the "Default" field being checked/unchecked
    // Does DPS need to do anything with the "Default" field?
    // Will we ever have race conditions? E.g. Creation/Update being separate if the user quickly uploads then clicks "default" in NOMIS
    return DistinguishingMarkImageSyncResponse(UUID.fromString("3b47dc4c-5d61-434e-ae7c-29e10718e780"))
  }

  @DeleteMapping("/{distinguishingMarkId}/images/{imageId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC_RW')")
  @Operation(
    summary = "SYNC endpoint to sync distinguishing mark image deletions made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO SYNC CHANGES MADE IN NOMIS.</b> " +
      "There is a separate endpoint for normal deletion of the distinguishing mark images." +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__DISTINGUISHING_MARKS_SYNC__RW`" +
      "<br/><br/>Distinguishing mark images can be deleted in NOMIS on both the current booking " +
      "and to old bookings. This sync API can handle both by accepting the `appliesFrom` and " +
      "`appliesTo` timestamps - `appliesFrom` should be equal to the booking start date and and " +
      "`appliesTo` should be equal to the booking end date. The `createdAt` date should always be " +
      "the point in time that the user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns UUID of created distinguishing mark image",
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
  fun syncDeleteDistinguishingMarkImage(
    @PathVariable prisonerNumber: String,
    @PathVariable distinguishingMarkId: String,
    @PathVariable imageId: String,
  ): DistinguishingMarkImageSyncResponse {
    // Hard or soft delete? Image history will be in the history table so soft delete is possible
    // If you delete something from a historical booking, what will that look like?
    return DistinguishingMarkImageSyncResponse(UUID.fromString("3b47dc4c-5d61-434e-ae7c-29e10718e780"))
  }
}