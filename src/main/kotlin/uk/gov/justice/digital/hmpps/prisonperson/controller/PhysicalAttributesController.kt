package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.swagger.v3.oas.annotations.Operation
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonperson.config.PrisonPersonDataNotFoundException
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PhysicalAttributesUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.service.PhysicalAttributesService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(
  name = "Physical Attributes",
  description = "The physical attributes of a prisoner - height, weight, hair, facial hair, face shape, build, eye colour and shoe size.",
)
@RequestMapping("/prisoners/{prisonerNumber}/physical-attributes", produces = [MediaType.APPLICATION_JSON_VALUE])
class PhysicalAttributesController(private val physicalAttributesService: PhysicalAttributesService) {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO')")
  @Operation(
    description = "Requires role `ROLE_PRISON_PERSON_API__PRISON_PERSON_DATA__RO`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the prisoner's physical attributes",
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
  fun getPhysicalAttributes(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
  ) = physicalAttributesService.getPhysicalAttributes(prisonerNumber)
    ?: throw PrisonPersonDataNotFoundException(prisonerNumber)

  @PatchMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW')")
  @Operation(
    summary = "Updates the physical attributes (height and weight) for a prisoner",
    description = "Requires role `ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW`",
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
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__PHYSICAL_ATTRIBUTES__RW",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun setPhysicalAttributes(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Valid
    physicalAttributesUpdateRequest: PhysicalAttributesUpdateRequest,
  ): PhysicalAttributesDto = physicalAttributesService.createOrUpdate(prisonerNumber, physicalAttributesUpdateRequest)
}
