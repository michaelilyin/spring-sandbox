package com.example.springsandbox.reposotory.cascade.removal

import com.example.springsandbox.entity.cascade.removal.BarForBiWoCascadeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarForBiWoCascadeRepository : JpaRepository<BarForBiWoCascadeEntity, Long> {
    fun findAllByFooId(fooId: Long): List<BarForBiWoCascadeEntity>
}
