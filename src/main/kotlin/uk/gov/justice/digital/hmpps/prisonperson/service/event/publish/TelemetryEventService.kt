package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener

@Service
class TelemetryEventService(
  private val telemetryClient: TelemetryClient,
) {
  @TransactionalEventListener(phase = AFTER_COMMIT)
  fun <T> publishTelemetryEvent(event: PrisonPersonEvent<T>) {
    event.getTelemetryEvent()?.run { telemetryClient.trackEvent(this.name, this.properties, null) }
  }
}
