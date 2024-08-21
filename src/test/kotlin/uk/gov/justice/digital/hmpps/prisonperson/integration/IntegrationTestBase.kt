package uk.gov.justice.digital.hmpps.prisonperson.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
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
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.prisonperson.config.CLIENT_ID
import uk.gov.justice.digital.hmpps.prisonperson.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.integration.testcontainers.LocalStackContainer
import uk.gov.justice.digital.hmpps.prisonperson.integration.testcontainers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.DocumentServiceExtension
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonperson.integration.wiremock.PrisonerSearchExtension
import uk.gov.justice.digital.hmpps.prisonperson.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.prisonperson.jpa.repository.utils.expectNoFieldHistoryFor
import uk.gov.justice.digital.hmpps.prisonperson.service.event.DomainEvent
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.MissingTopicException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(HmppsAuthApiExtension::class, PrisonerSearchExtension::class, DocumentServiceExtension::class)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @SpyBean
  lateinit var hmppsQueueService: HmppsQueueService

  protected val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("domainevents") ?: throw MissingTopicException("domainevents not found")
  }

  // sqs queue subscribed to the topic for testing publishing events
  protected val publishTestQueue by lazy {
    hmppsQueueService.findByQueueId("publishtest") ?: throw MissingQueueException("publishtest not found")
  }

  // sqs queue subscribed to the topic for testing publishing events
  protected val prisonPersonQueue by lazy {
    hmppsQueueService.findByQueueId("prisonperson") ?: throw MissingQueueException("prisonperson not found")
  }

  @BeforeEach
  fun `clear queues`() {
    publishTestQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(publishTestQueue.queueUrl).build()).get()
    prisonPersonQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(prisonPersonQueue.queueUrl).build()).get()

    await untilCallTo { publishTestQueue.countAllMessagesOnQueue() } matches { it == 0 }
    await untilCallTo { prisonPersonQueue.countAllMessagesOnQueue() } matches { it == 0 }
  }

  protected fun setAuthorisation(
    user: String? = null,
    client: String = CLIENT_ID,
    roles: List<String> = listOf(),
    isUserToken: Boolean = true,
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, client, roles, isUserToken = isUserToken)

  protected fun HmppsQueue.countAllMessagesOnQueue(): Int =
    sqsClient.countAllMessagesOnQueue(queueUrl).get()

  final fun HmppsQueue.receiveMessageOnQueue(): Message =
    sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build()).get().messages().single()

  final inline fun <reified T> HmppsQueue.receiveDomainEventOnQueue() =
    receiveMessageOnQueue()
      .let { objectMapper.readValue<MsgBody>(it.body()) }
      .let { objectMapper.readValue<DomainEvent<T>>(it.Message) }

  protected fun jsonString(any: Any) = objectMapper.writeValueAsString(any) as String

  protected fun <T> expectFieldHistory(field: PrisonPersonField, vararg comparison: HistoryComparison<T>) =
    expectFieldHistory(field, fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER), *comparison)

  protected fun expectNoFieldHistoryFor(vararg field: PrisonPersonField) {
    val history = fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER)
    field.forEach { expectNoFieldHistoryFor(it, history) }
  }

  protected fun expectFieldMetadata(prisonerNumber: String, vararg comparison: FieldMetadata) {
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(prisonerNumber)).containsExactlyInAnyOrder(*comparison)
  }

  protected fun expectFieldMetadata(vararg comparison: FieldMetadata) = expectFieldMetadata(PRISONER_NUMBER, *comparison)

  @JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy::class)
  data class MsgBody(val Message: String)

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  private companion object {
    private val PRISONER_NUMBER = "A1234AA"

    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }
}
