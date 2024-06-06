package uk.gov.justice.digital.hmpps.prisonperson

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HmppsPrisonPersonApi

fun main(args: Array<String>) {
  runApplication<HmppsPrisonPersonApi>(*args)
}
