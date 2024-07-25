package uk.gov.justice.digital.hmpps.prisonperson.service.merge

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PrisonPersonMergeService(
  private val physicalAttributesMergeService: PhysicalAttributesMergeService,
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
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
