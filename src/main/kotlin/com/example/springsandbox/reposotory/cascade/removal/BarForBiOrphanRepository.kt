package com.example.springsandbox.reposotory.cascade.removal

import com.example.springsandbox.entity.cascade.removal.BarForBiOrphanEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarForBiOrphanRepository : JpaRepository<BarForBiOrphanEntity, Long> {
    fun findAllByFooId(fooId: Long): List<BarForBiOrphanEntity>
}
