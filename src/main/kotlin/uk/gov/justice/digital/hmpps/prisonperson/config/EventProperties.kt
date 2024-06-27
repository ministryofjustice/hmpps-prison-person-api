package uk.gov.justice.digital.hmpps.prisonperson.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "events")
data class EventProperties(val publish: Boolean, val baseUrl: String)
