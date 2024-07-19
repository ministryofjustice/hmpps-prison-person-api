package uk.gov.justice.digital.hmpps.prisonperson.integration.testcontainers

import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket

object PostgresContainer {
  val instance: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? {
    if (isPostgresRunning()) {
      log.warn("Using existing Postgres database")
      return null
    }
    log.info("Creating a Postgres database")
    return PostgreSQLContainer<Nothing>("postgres").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withEnv("PORT_EXTERNAL", "5432")
      withDatabaseName("prison-person-data")
      withUsername("prison-person-data")
      withPassword("prison-person-data")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)

      start()
    }
  }

  private fun isPostgresRunning(): Boolean =
    try {
      val serverSocket = ServerSocket(5432)
      serverSocket.localPort == 0
    } catch (e: IOException) {
      true
    }

  private val log = LoggerFactory.getLogger(this::class.java)
}
