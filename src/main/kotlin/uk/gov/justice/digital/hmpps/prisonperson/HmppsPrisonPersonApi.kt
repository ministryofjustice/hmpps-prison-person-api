package uk.gov.justice.digital.hmpps.prisonperson

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.prisonperson.config.EventProperties

const val SYSTEM_USERNAME = "PRISON_PERSON_API"

@SpringBootApplication
@EnableConfigurationProperties(EventProperties::class)
class HmppsPrisonPersonApi

fun main(args: Array<String>) {
  runApplication<HmppsPrisonPersonApi>(*args)
}
