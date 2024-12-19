package uk.gov.justice.digital.hmpps.prisonperson.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonperson.dto.history.DistinguishingMarkHistoryDto

@Converter
@Component
class DistinguishingMarkConverter(val objectMapper: ObjectMapper) : AttributeConverter<DistinguishingMarkHistoryDto, String> {
  override fun convertToDatabaseColumn(distinguishingMark: DistinguishingMarkHistoryDto?): String? =
    distinguishingMark?.let { objectMapper.writeValueAsString(it) }

  override fun convertToEntityAttribute(json: String?): DistinguishingMarkHistoryDto? =
    json?.let { objectMapper.readValue(it, DistinguishingMarkHistoryDto::class.java) }
}
