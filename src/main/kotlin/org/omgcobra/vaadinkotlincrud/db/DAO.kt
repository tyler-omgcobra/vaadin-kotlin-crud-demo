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
                               private val entityType: KClass<T>) : ProvidesData<T, Map<String, String>> {

    private val table = entityType.findAnnotation<Table>()!!.value

    private val dataProvider by lazy {
        DataProvider.fromFilteringCallbacks<T, Map<String, String>>(this::filter, this::count)
    }

    override fun provide() = dataProvider

    private fun createEntity(supplier: (@ParameterName("property") KProperty<*>) -> Any?): T {
        val constructor = entityType.primaryConstructor ?: throw IllegalStateException("No primary constructor")
        val entity = constructor.callBy(mapOf(
                *entityType.keyColumnProperties.map {
                    (constructor.findParameterByName(it.name)
                            ?: throw IllegalStateException("Key not in primary constructor")) to supplier(it)
                }.toTypedArray()
        ))
        return entity.apply {
            entityType.columnProperties.forEach { set(it.name, supplier(it)) }
        }
    }

    private fun createEntity(resultSet: ResultSet, rowNum: Int): T? {
        return createEntity { resultSet.getObject(it.columnName, (it.returnType.classifier as KClass<*>).javaObjectType) }
    }

    private fun updateEntity(thing: T): T {
        val properties = thing.nonKeys
        val keys = thing.keys

        val sets = properties.joinToString(separator = ", ") { "${it.name} = ?" }
        val whereKeys = keys.joinToString(separator = " AND ") { "${it.name} = ?" }

        template.update("""UPDATE $table
                         | SET $sets
                         | WHERE $whereKeys""".trimMargin(),
                *properties.map { it.value }.toTypedArray(),
                *keys.map { it.value }.toTypedArray())

        dataProvider.refreshItem(thing)

        return thing
    }

    private fun insertEntity(thing: T): T {
        val map = thing.nonKeys
        val columns = map.joinToString(separator = ", ") { it.name }
        val values = map.joinToString(separator = ", ") { "?" }
        val keys = thing.keys.map { it.name }

        val keyHolder = GeneratedKeyHolder()
        template.update({ con ->
            val statement = con.prepareStatement(
                    """INSERT INTO $table($columns)
                     | VALUES ($values)""".trimMargin(), keys.toTypedArray())

            var n = 1

            map.forEach { statement.setObject(n++, it.value) }

            statement
        }, keyHolder)

        dataProvider.refreshAll()

        return createEntity { keyHolder.keys?.get(it.name) ?: thing[it.name] }
    }

    fun saveEntity(thing: T): T {
        val existing = thing.keys.all { it.value != null }

        return if (existing) updateEntity(thing) else insertEntity(thing)
    }

    private fun getColumnName(property: String) = entityType.memberProperties
            .first { it.name.equals(property, ignoreCase = true) }.columnName

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

interface ProvidesData<T : Entity, F : Any> {
    fun provide(): DataProvider<T, F>
}

@Repository
class PersonDAO(template: JdbcTemplate) : DAO<Person>(template, Person::class) {
    fun save(thing: Person) = saveEntity(thing)
}

@Repository
class ValueDAO(template: JdbcTemplate) : DAO<Basic>(template, Basic::class) {
    fun save(thing: Basic) = saveEntity(thing)
}