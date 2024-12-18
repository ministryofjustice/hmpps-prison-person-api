package uk.gov.justice.digital.hmpps.prisonperson.service.event.subscribe

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.digital.hmpps.prisonperson.service.event.PrisonerMergedAdditionalInformation
import uk.gov.justice.digital.hmpps.prisonperson.service.merge.PrisonPersonMergeService

@Service
@ConditionalOnProperty(prefix = "events", name = ["subscribe"], havingValue = "true")
class DomainEventListener(
  private val prisonPersonMergeService: PrisonPersonMergeService,
  private val mapper: ObjectMapper,
) {
  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  @SqsListener("prisonperson", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(requestJson: String) {
    val (message, messageAttributes) = mapper.readValue(requestJson, Message::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    when (eventType) {
      "prison-offender-events.prisoner.merged" -> {
        val typeRef = object : TypeReference<DomainEvent<PrisonerMergedAdditionalInformation>>() {}
        val domainEvent = mapper.readValue(message, typeRef)

        domainEvent.additionalInformation?.let {
          if (domainEvent.additionalInformation.reason == "MERGE") {
            prisonPersonMergeService.mergeRecords(
              domainEvent.additionalInformation.removedNomsNumber,
              domainEvent.additionalInformation.nomsNumber,
            )
          } else {
            log.warn("Ignoring event with reason ${domainEvent.additionalInformation.reason}")
          }
        } ?: { log.warn("Ignoring event without additionalInformation") }
      }
      else -> {
        log.debug("Ignoring message with type $eventType")
      }
    }
  }
}

data class EventType(val Value: String, val Type: String)
data class MessageAttributes(val eventType: EventType)
data class Message(
  val Message: String,
  val MessageAttributes: MessageAttributes,
)
