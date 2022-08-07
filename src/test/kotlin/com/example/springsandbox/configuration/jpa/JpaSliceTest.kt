package com.example.springsandbox.configuration.jpa

import com.example.springsandbox.configuration.containers.PostgresContainer
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql

@ComponentScan(basePackageClasses = [JpaSliceTest::class], lazyInit = true)
@DataJpaTest(showSql = true)
@Sql(
    scripts = ["/test-cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@ContextConfiguration(initializers = [JpaSlicePostgresInitializer::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
annotation class JpaSliceTest {
}

class JpaSlicePostgresInitializer : PostgresContainer.Initializer(liquibase = true, jdbc = true)
