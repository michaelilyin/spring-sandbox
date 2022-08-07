package com.example.springsandbox.reposotory.cascade.removal

import com.example.springsandbox.entity.cascade.removal.BarEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarRepository : JpaRepository<BarEntity, Long> {
    fun findAllByFooId(fooId: Long): List<BarEntity>
}
