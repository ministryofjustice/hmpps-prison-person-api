package uk.gov.justice.digital.hmpps.prisonperson.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpHeaders
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.prisonperson.config.CLIENT_ID
import uk.gov.justice.digital.hmpps.prisonperson.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.prisonperson.integration.testcontainers.LocalStackContainer
import uk.gov.justice.digital.hmpps.prisonperson.integration.testcontainers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PrisonerSearchExtension
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.MissingTopicException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(HmppsAuthApiExtension::class, PrisonerSearchExtension::class)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @SpyBean
  lateinit var hmppsQueueService: HmppsQueueService

  internal val hmppsEventTopic by lazy { hmppsQueueService.findByTopicId("hmppseventtopic") ?: throw MissingTopicException("hmppseventtopic not found") }

  // sqs queue subscribed to the topic purely for testing events
  internal val hmppsEventsQueue by lazy { hmppsQueueService.findByQueueId("hmppseventtestqueue") ?: throw MissingQueueException("hmppseventtestqueue queue not found") }

  @BeforeEach
  fun `clear queues`() {
    hmppsEventsQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(hmppsEventsQueue.queueUrl).build()).get()
  }

  internal fun setAuthorisation(
    user: String? = null,
    client: String = CLIENT_ID,
    roles: List<String> = listOf(),
    isUserToken: Boolean = true,
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, client, roles, isUserToken = isUserToken)

  internal fun HmppsQueue.countAllMessagesOnQueue() =
    sqsClient.countAllMessagesOnQueue(queueUrl).get()

  internal fun HmppsQueue.receiveMessageOnQueue() =
    sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build()).get().messages().single()

  internal fun HmppsQueue.receiveDomainEventOnQueue() =
    receiveMessageOnQueue()
      .let { objectMapper.readValue<MsgBody>(it.body()) }
      .let { objectMapper.readValue<DomainEvent>(it.Message) }

  @JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy::class)
  private data class MsgBody(val Message: String)

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  companion object {
    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }
}
