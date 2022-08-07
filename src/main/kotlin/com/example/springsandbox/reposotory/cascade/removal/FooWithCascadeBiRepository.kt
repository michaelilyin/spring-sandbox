package com.example.springsandbox.reposotory.cascade.removal

import com.example.springsandbox.entity.cascade.removal.FooWithCascadeBiEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FooWithCascadeBiRepository : JpaRepository<FooWithCascadeBiEntity, Long> {
}
