package uk.gov.justice.digital.hmpps.prisonperson.jpa.repository

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlMergeMode
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonperson.integration.TestBase

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlMergeMode(MERGE)
@Sql("classpath:jpa/repository/reset.sql")
abstract class RepositoryTest : TestBase()
