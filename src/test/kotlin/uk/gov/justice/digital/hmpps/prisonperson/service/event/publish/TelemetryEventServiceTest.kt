package uk.gov.justice.digital.hmpps.prisonperson.service.event.publish

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.prisonperson.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PHYSICAL_ATTRIBUTES_MIGRATED
import uk.gov.justice.digital.hmpps.prisonperson.enums.EventType.PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED
import uk.gov.justice.digital.hmpps.prisonperson.service.event.TelemetryEvent

class TelemetryEventServiceTest {
  lateinit var telemetryClient: TelemetryClient
  lateinit var prisonerSearchClient: PrisonerSearchClient
  lateinit var telemetryEventService: TelemetryEventService

  @BeforeEach
  fun setUp() {
    telemetryClient = mock<TelemetryClient>()
    prisonerSearchClient = mock<PrisonerSearchClient>()
    telemetryEventService = TelemetryEventService(telemetryClient, prisonerSearchClient)
  }

  @Test
  fun `makes call to telemetry client to track event (without including prisonId)`() {
    telemetryEventService.publishTelemetryEvent(TestEvent(EVENT_WITHOUT_PRISON_ID, PRISONER_NUMBER))

    verify(telemetryClient).trackEvent(EVENT_NAME, EVENT_PROPERTIES, null)
  }

  @Test
  fun `makes call to telemetry client to track event (including prisonId)`() {
    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(PrisonerDto(PRISONER_NUMBER, PRISON_ID))

    telemetryEventService.publishTelemetryEvent(TestEvent(EVENT_WITH_PRISON_ID, PRISONER_NUMBER))

    verify(telemetryClient).trackEvent(
      EVENT_NAME,
      buildMap {
        putAll(EVENT_PROPERTIES)
        put("prisonId", PRISON_ID)
      },
      null,
    )
  }

  @Test
  fun `handles a null response from the prisonerSearchClient`() {
    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)

    telemetryEventService.publishTelemetryEvent(TestEvent(EVENT_WITH_PRISON_ID, PRISONER_NUMBER))

    verify(telemetryClient).trackEvent(EVENT_NAME, EVENT_PROPERTIES, null)
  }

  @Test
  fun `handles a null prisonId response from the prisonerSearchClient`() {
    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(PrisonerDto(PRISONER_NUMBER, prisonId = null))

    telemetryEventService.publishTelemetryEvent(TestEvent(EVENT_WITH_PRISON_ID, PRISONER_NUMBER))

    verify(telemetryClient).trackEvent(EVENT_NAME, EVENT_PROPERTIES, null)
  }

  @Test
  fun `handles an exception thrown by prisonerSearchClient - continue without the prisonId`() {
    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenThrow(RuntimeException("some error"))

    telemetryEventService.publishTelemetryEvent(TestEvent(EVENT_WITH_PRISON_ID, PRISONER_NUMBER))

    verify(telemetryClient).trackEvent(EVENT_NAME, EVENT_PROPERTIES, null)
  }

  private class TestEvent(override val eventType: EventType, override val prisonerNumber: String) : PrisonPersonEvent<Void> {
    override fun getTelemetryEvent(): TelemetryEvent = TelemetryEvent(EVENT_NAME, EVENT_PROPERTIES)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISON_ID = "MDI"
    const val EVENT_NAME = "event-name"
    val EVENT_WITHOUT_PRISON_ID = PHYSICAL_ATTRIBUTES_MIGRATED
    val EVENT_WITH_PRISON_ID = PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED
    val EVENT_PROPERTIES = mapOf("a" to "b")
  }
}
