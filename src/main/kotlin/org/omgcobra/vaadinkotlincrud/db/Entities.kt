package org.omgcobra.vaadinkotlincrud.db

import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Target(AnnotationTarget.PROPERTY)
annotation class Key

@Target(AnnotationTarget.PROPERTY)
annotation class Column(val value: String)

@Target(AnnotationTarget.CLASS)
annotation class Table(val value: String)

val <E : Entity> KClass<out E>.columnProperties: List<KProperty1<out E, Any?>>
    get() = memberProperties.filter { it.findAnnotation<Column>() != null }

val <E : Entity> KClass<out E>.keyColumnProperties: List<KProperty1<out E, Any?>>
    get() = columnProperties.filter { it.findAnnotation<Key>() != null }

val <E : Entity> KClass<out E>.nonKeyColumnProperties: List<KProperty1<out E, Any?>>
    get() = columnProperties.filter { it.findAnnotation<Key>() == null }

val <P : KProperty<*>> P.columnName : String?
    get() = findAnnotation<Column>()?.value

val <E : Entity> E.keys: List<PropertyResult>
    get() = this::class.keyColumnProperties.map { PropertyResult(it.columnName!!, it.getter.call(this)) }

val <E : Entity> E.nonKeys: List<PropertyResult>
    get() = this::class.nonKeyColumnProperties.map { PropertyResult(it.columnName!!, it.getter.call(this)) }

data class PropertyResult(val name: String, val value: Any?)

fun <E : Entity> KClass<out E>.create(supplier: (@ParameterName("property") KProperty<*>) -> Any?): E {
    val constructor = primaryConstructor ?: throw IllegalStateException("No primary constructor")
    val entity = constructor.callBy(mapOf(
            *keyColumnProperties.map {
                (constructor.findParameterByName(it.name)
                        ?: throw IllegalStateException("Key not in primary constructor")) to supplier(it)
            }.toTypedArray()
    ))
    return entity.apply {
        columnProperties.forEach { set(it.name, supplier(it)) }
    }
}


interface Entity {
    operator fun get(name: String): Any? {
        return this::class.memberProperties.first { it.name.equals(name, ignoreCase = true) }.getter.call(this)
    }

    operator fun set(name: String, value: Any?) {
        when (val property = this::class.memberProperties.first { it.name.equals(name, ignoreCase = true) }) {
            is KMutableProperty<*> -> property.setter.call(this, value)
        }
    }
}

@Table("vals")
data class Basic(@Key @Column("id") val differentNamedId: Int? = null) : Entity {
    @Column("value") var value: String = ""
    @Column("isit") var truthy: Boolean = false
    @Column("floaty") var floaty: Float = 0.0f
}

@Table("person")
data class Person(@Key @Column("id") val id: Int? = null) : Entity {
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