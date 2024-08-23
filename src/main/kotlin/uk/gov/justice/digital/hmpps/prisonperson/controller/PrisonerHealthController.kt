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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonperson.dto.request.PrisonerHealthUpdateRequest
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PrisonerHealthDto
import uk.gov.justice.digital.hmpps.prisonperson.service.PrisonerHealthService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Health", description = "The health information for a prisoner")
@RequestMapping("/prisoners/{prisonerNumber}/health", produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonerHealthController(private val prisonerHealthService: PrisonerHealthService) {
  @PatchMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__HEALTH__RW')")
  @Operation(
    summary = "Updates the health information for a prisoner",
    description = "Requires role `ROLE_PRISON_PERSON_API__HEALTH__RW`",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Returns prisoner's health information",
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
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__HEALTH__RW",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun createOrUpdateHealth(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Valid
    prisonerHealthUpdateRequest: PrisonerHealthUpdateRequest,
  ): PrisonerHealthDto = prisonerHealthService.createOrUpdate(prisonerNumber, prisonerHealthUpdateRequest)
}
