package com.example.springsandbox.configuration.containers

import com.github.dockerjava.api.model.PortBinding
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainer(
    private val staticPortMapping: Int = 55432,
    database: String = "test",
    username: String = "test",
    password: String = "test"
) : PostgreSQLContainer<PostgresContainer>("postgres:14.3") {
    init {
        withDatabaseName(database)
        withUsername(username)
        withPassword(password)
        withCreateContainerCmdModifier {
            it.hostConfig?.withPortBindings(
                PortBinding.parse("$staticPortMapping:$POSTGRESQL_PORT")
            )
        }
    }

    open class Initializer(
        private val liquibase: Boolean = false,
        private val jdbc: Boolean = false,
        private val r2dbc: Boolean = false
    ) : ApplicationContextInitializer<ConfigurableApplicationContext> {

        private val postgres = PostgresContainer()

        override fun initialize(context: ConfigurableApplicationContext) {
            context.addApplicationListener(ContextClosedListener())
            postgres.start()

            val properties = mutableListOf<String>()
            if (liquibase) {
                properties += "spring.liquibase.url=${postgres.jdbcUrl}"
                properties += "spring.liquibase.user=${postgres.username}"
                properties += "spring.liquibase.password=${postgres.password}"
            }

            if (r2dbc) {
                properties += "spring.r2dbc.url=r2dbc:postgresql://" +
                        "${postgres.host}:${postgres.getMappedPort(POSTGRESQL_PORT)}/${postgres.databaseName}" +
                        "?schema=public"
                properties += "spring.r2dbc.username=${postgres.username}"
                properties += "spring.r2dbc.password=${postgres.password}"
            }

            if (jdbc) {
                properties += "spring.datasource.url=jdbc:postgresql://" +
                        "${postgres.host}:${postgres.getMappedPort(POSTGRESQL_PORT)}/${postgres.databaseName}" +
                        "?schema=public"
                properties += "spring.datasource.username=${postgres.username}"
                properties += "spring.datasource.password=${postgres.password}"
            }

            TestPropertyValues.of(properties)
                .applyTo(context.environment)
        }

        inner class ContextClosedListener: ApplicationListener<ContextClosedEvent> {
            override fun onApplicationEvent(event: ContextClosedEvent) {
                postgres.stop()
            }
        }
    }
}
