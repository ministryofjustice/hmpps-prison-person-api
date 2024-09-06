package uk.gov.justice.digital.hmpps.prisonperson.service.merge

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.config.trackEvent

@Service
class PrisonPersonMergeService(
  private val physicalAttributesMergeService: PhysicalAttributesMergeService,
  private val telemetryClient: TelemetryClient,
) : PrisonPersonMerge {

  @Transactional
  override fun mergeRecords(
    prisonerNumberFrom: String,
    prisonerNumberTo: String,
  ) {
    log.info("Merging prison person data from prisoner: '$prisonerNumberFrom', into prisoner: '$prisonerNumberTo'")

    listOf(
      physicalAttributesMergeService,
    ).forEach {
      it.mergeRecords(prisonerNumberFrom, prisonerNumberTo)
    }

    telemetryClient.trackEvent(
      "prison-person-api-merge-complete",
      mapOf(
        "prisonerNumberFrom" to prisonerNumberFrom,
        "prisonerNumberTo" to prisonerNumberTo,
      ),
    )
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
