package com.example.springsandbox.configuration.jpa

import com.example.springsandbox.tools.tx.Tx
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan(
    basePackageClasses = [
        Tx::class
    ]
)
@TestConfiguration
class JpaTestConfiguration {
}
