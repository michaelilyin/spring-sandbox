package com.example.springsandbox.reposotory.cascade.removal

import com.example.springsandbox.entity.cascade.removal.FooWoCascadeBiEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FooWoCascadeBiRepository : JpaRepository<FooWoCascadeBiEntity, Long> {
}
