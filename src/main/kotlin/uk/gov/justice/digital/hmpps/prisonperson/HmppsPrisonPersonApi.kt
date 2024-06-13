package uk.gov.justice.digital.hmpps.prisonperson

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

const val SYSTEM_USERNAME = "PRISON_PERSON_API"

@SpringBootApplication
class HmppsPrisonPersonApi

fun main(args: Array<String>) {
  runApplication<HmppsPrisonPersonApi>(*args)
}
