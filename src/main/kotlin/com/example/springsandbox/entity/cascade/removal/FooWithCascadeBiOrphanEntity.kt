package com.example.springsandbox.entity.cascade.removal

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "foo")
data class FooWithCascadeBiOrphanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @Column(name = "name")
    val name: String,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "foo", orphanRemoval = true)
    val bars: MutableList<BarForBiOrphanEntity>
) {
    override fun toString(): String {
        return "FooWithCascadeBiOrphanEntity(id=$id, name='$name', bars=$bars)"
    }
}
