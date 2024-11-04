package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient

@Service
class TelemetryEventService(
  private val telemetryClient: TelemetryClient,
  private val prisonerSearchClient: PrisonerSearchClient,
) {
  @TransactionalEventListener(phase = AFTER_COMMIT)
  fun <T> publishTelemetryEvent(event: PrisonPersonEvent<T>) {
    event.getTelemetryEvent()?.run {
      telemetryClient.trackEvent(
        name,
        buildMap {
          putAll(properties)
          if (event.eventType.telemetryEventDetails?.addPrisonerPrisonId == true) {
            getPrisonerPrisonId(event.prisonerNumber)?.let { put("prisonId", it) }
          }
        },
        null,
      )
    }
  }

  private fun getPrisonerPrisonId(prisonerNumber: String): String? =
    try {
      prisonerSearchClient.getPrisoner(prisonerNumber)?.prisonId
    } catch (e: Exception) {
      log.warn("Unable to add prisonId to telemetry event for prisoner: $prisonerNumber", e)
      null
    }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
