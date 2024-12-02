package uk.gov.justice.digital.hmpps.prisonperson.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

@Converter
@Component
class DistinguishingMarkConverter(val objectMapper: ObjectMapper) : AttributeConverter<DistinguishingMark, String> {
  override fun convertToDatabaseColumn(distinguishingMark: DistinguishingMark?): String? =
    distinguishingMark?.let { objectMapper.writeValueAsString(it) }

  override fun convertToEntityAttribute(json: String?): DistinguishingMark? =
    json?.let { objectMapper.readValue(it, DistinguishingMark::class.java) }
}

