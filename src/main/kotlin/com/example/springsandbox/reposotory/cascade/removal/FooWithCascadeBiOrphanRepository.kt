package com.example.springsandbox.reposotory.cascade.removal

import com.example.springsandbox.entity.cascade.removal.FooWithCascadeBiOrphanEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FooWithCascadeBiOrphanRepository : JpaRepository<FooWithCascadeBiOrphanEntity, Long> {
}
