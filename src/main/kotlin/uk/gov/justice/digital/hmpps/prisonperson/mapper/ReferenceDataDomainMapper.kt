package uk.gov.justice.digital.hmpps.prisonperson.mapper

import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataDomainDto
import uk.gov.justice.digital.hmpps.prisonperson.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

fun ReferenceDataDomain.toDto(): ReferenceDataDomainDto = ReferenceDataDomainDto(
  code,
  description,
  listSequence,
  isActive(),
  createdAt,
  createdBy,
  lastModifiedAt,
  lastModifiedBy,
  deactivatedAt,
  deactivatedBy,
  referenceDataCodes.map { it.toDto() },
  subDomains.map { it.toDto() },
)

fun ReferenceDataDomain.isActive() = deactivatedAt?.isBefore(ZonedDateTime.now()) != true
