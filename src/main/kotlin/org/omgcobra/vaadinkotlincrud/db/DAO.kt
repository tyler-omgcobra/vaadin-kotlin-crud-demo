package org.omgcobra.vaadinkotlincrud.db

import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.lang.IllegalStateException
import java.sql.ResultSet
import java.util.stream.Stream
import kotlin.reflect.*
import kotlin.reflect.full.*

abstract class DAO<T : Entity>(private val template: JdbcTemplate,
                               private val entityType: KClass<T>) {

    private val table = entityType.findAnnotation<Table>()!!.value

    private val dataProvider by lazy {
        DataProvider.fromFilteringCallbacks<T, Map<String, String>>(this::filter, this::count)
    }

    private fun createEntity(supplier: (@ParameterName("property") KProperty<*>) -> Any?): T? {
        val entity = entityType.createInstance()
        entityType.memberProperties.filter { it.findAnnotation<Column>() != null }.forEach {
            entity[it.name] = supplier(it)
        }
        return entity
    }

    private fun createEntity(resultSet: ResultSet, rowNum: Int): T? {
        return createEntity { resultSet.getObject(getColumnName(it), (it.returnType.classifier as KClass<*>).javaObjectType) }
    }

    private fun isKey(element: KAnnotatedElement?) = element?.findAnnotation<Key>() != null

    private fun updateEntity(thing: T) {
        val properties = entityType.memberProperties.filter { !isKey(it) }.map {
            getColumnName(it) to it.getter.call(thing)
        }
        val keys = getKeyProperties().map {
            getColumnName(it) to it.getter.call(thing)
        }

        val sets = properties.joinToString(separator = ", ") { "${it.first} = ?" }
        val whereKeys = keys.joinToString(separator = " AND ") { "${it.first} = ?" }

        template.update("""UPDATE $table
                         | SET $sets
                         | WHERE $whereKeys""".trimMargin(),
                *properties.map { it.second }.toTypedArray(),
                *keys.map { it.second }.toTypedArray())

        dataProvider.refreshItem(thing)
    }

    private fun insertEntity(thing: T) {
        val map = entityType.memberProperties
                .filter { !isKey(it) }
                .map { getColumnName(it) to it.getter.call(thing) }
        val columns = map.joinToString(separator = ", ") { it.first }
        val values = map.joinToString(separator = ", ") { "?" }
        val keys = getKeyProperties().map { getColumnName(it) }

        val keyHolder = GeneratedKeyHolder()
        template.update({ con ->
            val statement = con.prepareStatement(
                    """INSERT INTO $table($columns)
                     | VALUES ($values)""".trimMargin(), keys.toTypedArray())

            var n = 1

            map.forEach { statement.setObject(n++, it.second) }

            statement
        }, keyHolder)

        keyHolder.keys?.forEach { (key, value) -> thing[key] = value }

        dataProvider.refreshAll()
    }

    fun saveEntity(thing: T) {
        val existing = getKeyProperties().all { it.getter.call(thing) != null }

        if (existing) updateEntity(thing) else insertEntity(thing)
    }

    private fun getKeyProperties() = entityType.memberProperties.filter { isKey(it) }
    private fun getProperty(property: String) = entityType.memberProperties.first { it.name.equals(property, ignoreCase = true) }
    private fun getColumnName(property: KProperty<*>) = property.findAnnotation<Column>()?.value ?: throw IllegalStateException("")
    private fun getColumnName(property: String) = getColumnName(getProperty(property))

    private fun filter(query: Query<T, Map<String, String>>) : Stream<T> {
        val where = where(query)
        return template.query("""SELECT *
                               | FROM $table
                               | ${where.clause}
                               | ${orderBy(query.sortOrders)}
                               | LIMIT ${query.limit}
                               | OFFSET ${query.offset}""".trimMargin(),
                RowMapper { rs, rowNum -> createEntity(rs, rowNum) },
                *where.values).stream()
    }

    private fun count(query: Query<T, Map<String, String>>): Int {
        val where = where(query)
        return template.queryForObject("""SELECT count(1)
                                        | FROM $table
                                        | ${where.clause}""".trimMargin(),
                Int::class.java,
                *where.values)
    }

    private fun orderBy(sortOrders: List<QuerySortOrder>): String {
        return when (sortOrders.size) {
            0 -> ""
            else -> sortOrders.joinToString(prefix = "ORDER BY ", separator = ", ") { order ->
                "${getColumnName(order.sorted)} ${order.direction.name.substringBefore("C")}C"
            }
        }
    }

    private fun where(query: Query<T, Map<String, String>>): Where {
        val filter = query.filter.orElse(emptyMap())

        return if (filter.isEmpty()) {
            Where("", emptyList())
        } else {
            val map = filter.map { getColumnName(it.key) to it.value }
            Where(map.joinToString(prefix = "WHERE ", separator = " AND ") { "${it.first} ILIKE ?" },
                    map.map { "%${it.second}%" })
        }
    }

    fun dataProvider(): DataProvider<T, Map<String, String>> = dataProvider
}

data class Where(val clause: String, private val valueList: List<Any>) {
    val values = valueList.toTypedArray()
}

interface ProvidesData<T : Entity> {
    fun provide(): DataProvider<T, Map<String, String>>
}

@Repository
class PersonDAO(template: JdbcTemplate) : DAO<Person>(template, Person::class), ProvidesData<Person> {
    override fun provide() = dataProvider()
    fun save(thing: Person) = saveEntity(thing)
}

@Repository
class ValueDAO(template: JdbcTemplate) : DAO<Basic>(template, Basic::class), ProvidesData<Basic> {
    override fun provide() = dataProvider()
    fun save(thing: Basic) = saveEntity(thing)
}