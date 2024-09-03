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
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.ProfileDetailsPhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ProfileDetailsPhysicalAttributesMigrationResponse
import uk.gov.justice.digital.hmpps.prisonperson.service.ProfileDetailsPhysicalAttributesMigrationService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.SortedSet

@RestController
@Tag(name = "Profile Details Physical Attributes")
@Tag(
  name = "Migration from NOMIS",
  description = "Endpoints to facilitate migration of data from NOMIS to the Prison Person database",
)
@RequestMapping(
  "/migration/prisoners/{prisonerNumber}/profile-details-physical-attributes",
  produces = [MediaType.APPLICATION_JSON_VALUE],
)
class ProfileDetailsPhysicalAttributesMigrationController(private val profileDetailsPhysicalAttributesMigrationService: ProfileDetailsPhysicalAttributesMigrationService) {

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION__RW')")
  @Operation(
    summary = "MIGRATION endpoint to sync changes to profile details physical attributes made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO MIGRATE DATA.</b>" +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION__RW` user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Returns a list of IDS of field history records created during the migration",
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
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATION__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun migratePersonalDetailsPhysicalAttributes(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Validated
    physicalAttributesMigration: SortedSet<ProfileDetailsPhysicalAttributesMigrationRequest>,
  ): ProfileDetailsPhysicalAttributesMigrationResponse =
    profileDetailsPhysicalAttributesMigrationService.migrate(prisonerNumber, physicalAttributesMigration)
}
