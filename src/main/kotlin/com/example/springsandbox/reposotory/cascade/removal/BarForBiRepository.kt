package com.example.springsandbox.reposotory.cascade.removal

import com.example.springsandbox.entity.cascade.removal.BarForBiEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarForBiRepository : JpaRepository<BarForBiEntity, Long> {
    fun findAllByFooId(fooId: Long): List<BarForBiEntity>
}
