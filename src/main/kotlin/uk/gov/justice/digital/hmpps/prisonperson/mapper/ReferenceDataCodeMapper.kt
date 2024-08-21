package uk.gov.justice.digital.hmpps.prisonperson.mapper

import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataCode
import java.time.ZonedDateTime

/*
 * Map some NOMIS descriptions to new DPS descriptions based on `domain` and `code`
 */
object ReferenceDataCodeMapper {
  val referenceDataCodeDescriptionMappings = mapOf(
    "HAIR" to mapOf(
      "MOUSE" to "Mousy",
    ),
    "FACIAL_HAIR" to mapOf(
      "CLEAN SHAVEN" to "No facial hair",
      "MOUSTACHE" to "Moustache",
    ),
    "FACE" to mapOf(
      "BULLET" to "Long",
      "TRIANGULAR" to "Triangle",
    ),
    "BUILD" to mapOf(),
  )
}

fun ReferenceDataCode.toDto(): ReferenceDataCodeDto {
  val mappedDescription = mapDescription(domain.code, code, description)
  return ReferenceDataCodeDto(
    id,
    domain = domain.code,
    code,
    description = mappedDescription,
    listSequence,
    isActive(),
    createdAt,
    createdBy,
    lastModifiedAt,
    lastModifiedBy,
    deactivatedAt,
    deactivatedBy,
  )
}

fun ReferenceDataCode.toSimpleDto(): ReferenceDataSimpleDto {
  val mappedDescription = mapDescription(domain.code, code, description)
  return ReferenceDataSimpleDto(
    id,
    description = mappedDescription,
    listSequence,
    isActive(),
  )
}

fun ReferenceDataCode.isActive() = deactivatedAt?.isBefore(ZonedDateTime.now()) != true

private fun mapDescription(domain: String, code: String, description: String): String =
  ReferenceDataCodeMapper.referenceDataCodeDescriptionMappings[domain]?.get(code) ?: description
