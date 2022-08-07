package com.example.springsandbox.entity.cascade.removal

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "bar")
data class BarForBiOrphanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "name")
    val name: String,

    @ManyToOne
    @JoinColumn(name = "foo_id")
    val foo: FooWithCascadeBiOrphanEntity
) {
    override fun toString(): String {
        return "BarForBiOrphanEntity(id=$id, name='$name', foo=${foo.id})"
    }
}
