package com.example.springsandbox.tools.tx

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ConcurrentHashMap

@Component
class Tx @Autowired constructor(
    private val transactionManager: PlatformTransactionManager
) {
    private val templates = ConcurrentHashMap<Key, TransactionTemplate>()

    operator fun <T> invoke(
        readOnly: Boolean = false,
        propagation: Propagation = Propagation.REQUIRED,
        isolation: Isolation = Isolation.READ_COMMITTED,
        block: () -> T
    ): T {
        val key = Key(readOnly, propagation, isolation)
        val template = computeTemplate(key)

        return template.execute { block() }!!
    }

    operator fun invoke(
        readOnly: Boolean = false,
        propagation: Propagation = Propagation.REQUIRED,
        isolation: Isolation = Isolation.READ_COMMITTED,
        block: () -> Unit
    ) {
        val key = Key(readOnly, propagation, isolation)
        val template = computeTemplate(key)

        template.executeWithoutResult { block() }
    }

    private fun computeTemplate(key: Key): TransactionTemplate = templates.computeIfAbsent(key) {
        val definition = DefaultTransactionDefinition()

        definition.propagationBehavior = it.propagation.value()
        definition.isolationLevel = it.isolation.value()
        definition.isReadOnly = it.readOnly

        TransactionTemplate(transactionManager, definition)
    }

    private data class Key(
        val readOnly: Boolean,
        val propagation: Propagation,
        val isolation: Isolation
    )
}
