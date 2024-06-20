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
import uk.gov.justice.digital.hmpps.prisonperson.dto.PhysicalAttributesMigrationRequest
import uk.gov.justice.digital.hmpps.prisonperson.service.PhysicalAttributesMigrationService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Physical Attributes", description = "The height and weight of a prisoner")
@Tag(name = "Migration from NOMIS", description = "Endpoints to facilitate migration of data from NOMIS to the Prison Person database")
@RequestMapping("/migration/prisoners/{prisonerNumber}/physical-attributes", produces = [MediaType.APPLICATION_JSON_VALUE])
class PhysicalAttributesMigrationController(private val physicalAttributesMigrationService: PhysicalAttributesMigrationService) {

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW')")
  @Operation(
    summary = "MIGRATION endpoint to sync changes to physical attributes made in NOMIS",
    description = "This endpoint <b>SHOULD ONLY BE USED IN ORDER TO MIGRATE DATA.</b>" +
      "<br/><br/>Requires role `ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW` user made the edit.",
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
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES_MIGRATION__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun migratePhysicalAttributes(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Validated
    physicalAttributesMigration: List<PhysicalAttributesMigrationRequest>,
  ): PhysicalAttributesDto = physicalAttributesMigrationService.migrate(prisonerNumber, physicalAttributesMigration)
}
