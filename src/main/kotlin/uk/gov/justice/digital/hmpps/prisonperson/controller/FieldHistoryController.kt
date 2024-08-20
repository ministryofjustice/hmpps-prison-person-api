package uk.gov.justice.digital.hmpps.prisonperson.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonperson.service.FieldHistoryService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(
  name = "Field History",
  description = "The history of changes to fields linked to a prisoner, such as height, weight, hair, facial hair, face shape, build, eye colour and shoe size.",
)
@RequestMapping("/prisoners/{prisonerNumber}/field-history", produces = [MediaType.APPLICATION_JSON_VALUE])
class FieldHistoryController(private val fieldHistoryService: FieldHistoryService) {

  @GetMapping("{field}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_PRISON_PERSON_API__FIELD_HISTORY__RO')")
  @Operation(
    description = "Requires role `ROLE_PRISON_PERSON_API__FIELD_HISTORY__RO`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns Field History Data",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_PRISON_PERSON_API__FIELD_HISTORY__RO",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getFieldHistory(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @Schema(description = "The field to get history for", example = "HAIR", required = true)
    @PathVariable
    field: String,
  ) = fieldHistoryService.getFieldHistory(prisonerNumber, field)
}
