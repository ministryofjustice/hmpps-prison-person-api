package uk.gov.justice.digital.hmpps.prisonperson.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.FieldHistoryDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.FieldHistoryRepository

@Service
@Transactional(readOnly = true)
class FieldHistoryService(
  private val fieldHistoryRepository: FieldHistoryRepository,
) {
  fun getFieldHistory(prisonerNumber: String, fieldName: String): Collection<FieldHistoryDto> {
    val prisonPersonField = PrisonPersonField.entries.find { it.name.equals(fieldName, ignoreCase = true) }
      ?: throw IllegalArgumentException("Invalid field: $fieldName")

    return fieldHistoryRepository.findAllByPrisonerNumberAndField(prisonerNumber, prisonPersonField).map { it.toDto() }
  }
}
