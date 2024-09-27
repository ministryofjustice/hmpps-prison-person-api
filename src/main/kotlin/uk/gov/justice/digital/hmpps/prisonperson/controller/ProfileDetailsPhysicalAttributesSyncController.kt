package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.ProfileDetailsPhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ProfileDetailsPhysicalAttributesSyncResponse
import uk.gov.justice.digital.hmpps.prisonperson.service.ProfileDetailsPhysicalAttributesSyncService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Profile Details Physical Attributes")
@Tag(
  name = "Sync with NOMIS",
  description = "Endpoints to keep the Prison Person database in sync with changes in the NOMIS database",
)
@RequestMapping(
  "/sync/prisoners/{prisonerNumber}/profile-details-physical-attributes",
  produces = [MediaType.APPLICATION_JSON_VALUE],
)
class ProfileDetailsPhysicalAttributesSyncController(private val profileDetailsPhysicalAttributesSyncService: ProfileDetailsPhysicalAttributesSyncService) {

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNC__RW')")
  @Operation(
    summary = "SYNC endpoint to sync changes to profile details physical attributes made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO SYNC CHANGES MADE IN NOMIS.</b> " +
      "There is a separate endpoint for normal editing of the Physical Attributes." +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNC__RW`" +
      "<br/><br/>Edits can be made to profile details physical attributes in NOMIS to both the current booking " +
      "and to old bookings. This sync API can handle both by accepting the `appliesFrom` and " +
      "`appliesTo` timestamps - `appliesFrom` should be equal to the point in time that the edit happened, and " +
      "`appliesTo` should be null.  For edits to historical bookings `appliesFrom` should be equal to the booking " +
      "start date and and `appliesTo` should be equal to the booking end date. The `lastModifiedAt` date should " +
      "always be the point in time that the user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Returns prisoner's physical attributes",
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
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNC__RW",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun syncPhysicalAttributes(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Validated
    profileDetailsPhysicalAttributesSyncRequest: ProfileDetailsPhysicalAttributesSyncRequest,
  ): ProfileDetailsPhysicalAttributesSyncResponse =
    profileDetailsPhysicalAttributesSyncService.sync(prisonerNumber, profileDetailsPhysicalAttributesSyncRequest)
}
