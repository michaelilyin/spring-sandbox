package com.example.springsandbox.jpa

import com.example.springsandbox.configuration.jpa.JpaSliceTest
import com.example.springsandbox.entity.cascade.removal.BarEntity
import com.example.springsandbox.entity.cascade.removal.BarForBiEntity
import com.example.springsandbox.entity.cascade.removal.BarForBiOrphanEntity
import com.example.springsandbox.entity.cascade.removal.BarForBiWoCascadeEntity
import com.example.springsandbox.entity.cascade.removal.FooEntity
import com.example.springsandbox.entity.cascade.removal.FooWithCascadeBiEntity
import com.example.springsandbox.entity.cascade.removal.FooWithCascadeBiOrphanEntity
import com.example.springsandbox.entity.cascade.removal.FooWoCascadeBiEntity
import com.example.springsandbox.reposotory.cascade.removal.BarForBiOrphanRepository
import com.example.springsandbox.reposotory.cascade.removal.BarForBiRepository
import com.example.springsandbox.reposotory.cascade.removal.BarForBiWoCascadeRepository
import com.example.springsandbox.reposotory.cascade.removal.BarRepository
import com.example.springsandbox.reposotory.cascade.removal.FooRepository
import com.example.springsandbox.reposotory.cascade.removal.FooWithCascadeBiOrphanRepository
import com.example.springsandbox.reposotory.cascade.removal.FooWithCascadeBiRepository
import com.example.springsandbox.reposotory.cascade.removal.FooWoCascadeBiRepository
import com.example.springsandbox.tools.tx.Tx
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import javax.transaction.Transactional

/**
 * Run test and check JPA logs for interesting cases.
 */
@JpaSliceTest
internal class RemovalsForManyToOneRelationship @Autowired constructor(
    private val fooRepository: FooRepository,
    private val barRepository: BarRepository,
    private val fooWithCascadeBiRepository: FooWithCascadeBiRepository,
    private val barForBiRepository: BarForBiRepository,
    private val fooWithCascadeBiOrphanRepository: FooWithCascadeBiOrphanRepository,
    private val barForBiOrphanRepository: BarForBiOrphanRepository,
    private val fooWoCascadeBiRepository: FooWoCascadeBiRepository,
    private val barForBiWoCascadeRepository: BarForBiWoCascadeRepository,
    private val tx: Tx
) {

    @Test
    @Transactional
    internal fun testOnlyManyToOneRemoval() {
        val fooId = tx<Long>(propagation = Propagation.REQUIRES_NEW) {
            val foo = FooEntity(0, "test-foo")
            fooRepository.save(foo)

            val bar1 = BarEntity(0, "test-bar-1", foo)
            val bar2 = BarEntity(0, "test-bar-2", foo)
            barRepository.save(bar1)
            barRepository.save(bar2)

            return@tx foo.id
        }

        val foo = fooRepository.findById(fooId).orElse(null)
        val bars = barRepository.findAllByFooId(fooId)
        assertThat(foo).isNotNull
        assertThat(bars).hasSize(2)

        tx(propagation = Propagation.REQUIRES_NEW) {
            barRepository.deleteAllById(bars.map { it.id })
        }

        val barsAfterRemoval = barRepository.findAllByFooId(fooId)
        assertThat(barsAfterRemoval).hasSize(0)
    }

    /**
     * This test represents bidirectional relationship with cascade-all but without orphan removal.
     */
    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "false, true, false", // this case shows that explicit deletion does not work - entities are recreated by flushing cascade collection
        "true, false, false" // this case shows that only cleanup of cascading collection also does not work without orphan removal attribute
    )
    internal fun testCascadeOneToManyWithBiDirection(
        cleanParentCollection: Boolean,
        explicitlyDelete: Boolean,
        deleted: Boolean
    ) {
        val fooId = tx<Long>(propagation = Propagation.REQUIRES_NEW) {
            val foo = FooWithCascadeBiEntity(0, "test-foo", mutableListOf())
            val saved = fooWithCascadeBiRepository.save(foo)

            val bar1 = BarForBiEntity(0, "test-bar-1", foo)
            val bar2 = BarForBiEntity(0, "test-bar-2", foo)
            barForBiRepository.save(bar1)
            barForBiRepository.save(bar2)

            assertThat(foo.bars).hasSize(0) // important note - bars haven't been added to the parent "hot" entity
            assertThat(saved.bars).hasSize(0) // important note - bars haven't been added to the parent "hot" entity

            return@tx foo.id
        }

        val foo = fooWithCascadeBiRepository.findById(fooId).orElse(null)
        val bars = barForBiRepository.findAllByFooId(fooId)
        assertThat(foo).isNotNull
        assertThat(foo.bars).hasSize(2)
        assertThat(bars).hasSize(2)

        tx(propagation = Propagation.REQUIRES_NEW) {
            val forClean = fooWithCascadeBiRepository.findById(fooId).orElse(null)
            val idForClean = forClean.bars.map { it.id }

            assertThat(idForClean).hasSize(2)
            if (cleanParentCollection) {
                forClean.bars.clear()
                fooWithCascadeBiRepository.save(forClean)
            }
            if (explicitlyDelete) {
                barForBiRepository.deleteAllById(idForClean)
            }
        }

        val barsAfterRemove = barForBiRepository.findAllByFooId(fooId)
        if (deleted) {
            assertThat(barsAfterRemove).hasSize(0)
        } else {
            assertThat(barsAfterRemove).hasSize(2)
        }
    }

    /**
     * This test represents bidirectional relationship with cascade-all but without orphan removal.
     * Note, that orphan removal is different to CascadeType.REMOVAL.
     * Cascade type works when parent entity is removed and orphan when child entity is removed from collection.
     */
    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "false, true, false", // this case shows that explicit deletion does not work - entities are recreated by flushing cascade collection
        "true, false, true" // this case is different to previous one - with orphan removal we do not have to explicitly delete dependant entities
    )
    internal fun testCascadeOneToManyWithBiDirectionWithOrphan(
        cleanParentCollection: Boolean,
        explicitlyDelete: Boolean,
        deleted: Boolean
    ) {
        val fooId = tx<Long>(propagation = Propagation.REQUIRES_NEW) {
            val foo = FooWithCascadeBiOrphanEntity(0, "test-foo", mutableListOf())
            val saved = fooWithCascadeBiOrphanRepository.save(foo)

            val bar1 = BarForBiOrphanEntity(0, "test-bar-1", foo)
            val bar2 = BarForBiOrphanEntity(0, "test-bar-2", foo)
            barForBiOrphanRepository.save(bar1)
            barForBiOrphanRepository.save(bar2)

            assertThat(foo.bars).hasSize(0) // important note - bars haven't been added to the parent "hot" entity
            assertThat(saved.bars).hasSize(0) // important note - bars haven't been added to the parent "hot" entity

            return@tx foo.id
        }

        val foo = fooWithCascadeBiOrphanRepository.findById(fooId).orElse(null)
        val bars = barForBiOrphanRepository.findAllByFooId(fooId)
        assertThat(foo).isNotNull
        assertThat(foo.bars).hasSize(2)
        assertThat(bars).hasSize(2)

        tx(propagation = Propagation.REQUIRES_NEW) {
            val forClean = fooWithCascadeBiOrphanRepository.findById(fooId).orElse(null)
            val idForClean = forClean.bars.map { it.id }

            assertThat(idForClean).hasSize(2)
            if (cleanParentCollection) {
                forClean.bars.clear()
                fooWithCascadeBiOrphanRepository.save(forClean)
            }
            if (explicitlyDelete) {
                barForBiOrphanRepository.deleteAllById(idForClean)
            }
        }

        val barsAfterRemove = barForBiOrphanRepository.findAllByFooId(fooId)
        if (deleted) {
            assertThat(barsAfterRemove).hasSize(0)
        } else {
            assertThat(barsAfterRemove).hasSize(2)
        }
    }

    /**
     * This test represents bidirectional relationship with cascade-all but without orphan removal.
     */
    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "false, true, true", // it is fine because PERSIST cascade type is removed (but it is lead to requirements of explicit creation)
        "true, false, false" // with orphan removal it will be true
    )
    internal fun testCascadeOneToManyWithBiDirectionWithoutCascade(
        cleanParentCollection: Boolean,
        explicitlyDelete: Boolean,
        deleted: Boolean
    ) {
        val fooId = tx<Long>(propagation = Propagation.REQUIRES_NEW) {
            val foo = FooWoCascadeBiEntity(0, "test-foo", mutableListOf())
            val saved = fooWoCascadeBiRepository.save(foo)

            val bar1 = BarForBiWoCascadeEntity(0, "test-bar-1", foo)
            val bar2 = BarForBiWoCascadeEntity(0, "test-bar-2", foo)
            barForBiWoCascadeRepository.save(bar1)
            barForBiWoCascadeRepository.save(bar2)

            assertThat(foo.bars).hasSize(0) // important note - bars haven't been added to the parent "hot" entity
            assertThat(saved.bars).hasSize(0) // important note - bars haven't been added to the parent "hot" entity

            return@tx foo.id
        }

        val foo = fooWoCascadeBiRepository.findById(fooId).orElse(null)
        val bars = barForBiWoCascadeRepository.findAllByFooId(fooId)
        assertThat(foo).isNotNull
        assertThat(foo.bars).hasSize(2)
        assertThat(bars).hasSize(2)

        tx(propagation = Propagation.REQUIRES_NEW) {
            val forClean = fooWoCascadeBiRepository.findById(fooId).orElse(null)
            val idForClean = forClean.bars.map { it.id }

            assertThat(idForClean).hasSize(2)
            if (cleanParentCollection) {
                forClean.bars.clear()
                fooWoCascadeBiRepository.save(forClean)
            }
            if (explicitlyDelete) {
                barForBiWoCascadeRepository.deleteAllById(idForClean)
            }
        }

        val barsAfterRemove = barForBiOrphanRepository.findAllByFooId(fooId)
        if (deleted) {
            assertThat(barsAfterRemove).hasSize(0)
        } else {
            assertThat(barsAfterRemove).hasSize(2)
        }
    }


}
