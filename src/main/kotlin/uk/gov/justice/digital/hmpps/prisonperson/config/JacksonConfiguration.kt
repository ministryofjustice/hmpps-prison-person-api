package uk.gov.justice.digital.hmpps.prisonperson.config

import org.openapitools.jackson.nullable.JsonNullableModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfiguration {
  @Bean
  fun jsonNullableModule(): JsonNullableModule {
    return JsonNullableModule()
  }
}
