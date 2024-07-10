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
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesSyncRequest
import uk.gov.justice.digital.hmpps.prisonperson.service.PhysicalAttributesSyncService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Physical Attributes", description = "The height and weight of a prisoner")
@Tag(name = "Sync with NOMIS", description = "Endpoints to keep the Prison Person database in sync with changes in the NOMIS database")
@RequestMapping("/sync/prisoners/{prisonerNumber}/physical-attributes", produces = [MediaType.APPLICATION_JSON_VALUE])
class PhysicalAttributesSyncController(private val physicalAttributesSyncService: PhysicalAttributesSyncService) {

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW')")
  @Operation(
    summary = "SYNC endpoint to sync changes to physical attributes made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO SYNC CHANGES MADE IN NOMIS.</b> " +
      "There is a separate endpoint for normal editing of the Physical Attributes." +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW`" +
      "<br/><br/>Edits can be made to physical attributes in NOMIS to both the current booking " +
      "and to old bookings. This sync API can handle both by accepting the `appliesFrom` and " +
      "`appliesTo` timestamps.  For edits to the current booking, `appliesFrom` should be equal to the " +
      "point in time that the edit happened, and `appliesTo` should be null.  For edits to historical " +
      "bookings `appliesFrom` should be equal to the booking start date and and `appliesTo` should be " +
      "equal to the booking end date. The `createdAt` date should always be the point in time that the " +
      "user made the edit.",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Returns prisoner's physical attributes",
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
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_SYNC__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun syncPhysicalAttributes(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Validated
    physicalAttributesSyncRequest: PhysicalAttributesSyncRequest,
  ): PhysicalAttributesDto = physicalAttributesSyncService.sync(prisonerNumber, physicalAttributesSyncRequest)
}
