package org.omgcobra.vaadinkotlincrud.db

import java.lang.IllegalStateException
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.PROPERTY)
annotation class Key

@Target(AnnotationTarget.PROPERTY)
annotation class Column(val value: String)

@Target(AnnotationTarget.CLASS)
annotation class Table(val value: String)

interface Entity {
    operator fun get(name: String): Any? {
        return this::class.memberProperties.first { it.name.equals(name, ignoreCase = true) }.getter.call(this)
    }

    operator fun set(name: String, value: Any?) {
        when (val property = this::class.memberProperties.first { it.name.equals(name, ignoreCase = true) }) {
            is KMutableProperty<*> -> property.setter.call(this, value)
            else -> throw IllegalStateException("")
        }
    }
}

@Table("vals")
data class Basic(@Key @Column("id") val differentNamedId: Int? = null) : Entity {
    @Column("value") val value: String = ""
    @Column("isit") val truthy: Boolean = false
    @Column("floaty") val floaty: Float = 0.0f
}

@Table("person")
data class Person(@Key @Column("id") var id: Int? = null) : Entity {
    @Column("first_name") var firstName: String = ""
    @Column("last_name") var lastName: String = ""
    @Column("street") var street: String = ""
    @Column("city") var city: String = ""
    @Column("state") var state: String = ""
    @Column("zip") var zip: String = ""
    @Column("email") var address: String = ""
    @Column("area_code") var areaCode: String = ""
    @Column("first_three") var firstThree: String = ""
    @Column("last_four") var lastFour: String = ""
}