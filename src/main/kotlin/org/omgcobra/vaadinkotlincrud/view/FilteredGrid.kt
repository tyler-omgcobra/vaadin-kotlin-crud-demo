package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode
import org.omgcobra.vaadinkotlincrud.db.Entity
import org.omgcobra.vaadinkotlincrud.db.ProvidesData
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class FilteredGrid<T : Entity>(provider: ProvidesData<T, Map<String, String>>, beanType: KClass<T>): Grid<T>(beanType.java, false) {

    private val wrapper = provider.provide().withConfigurableFilter()

    init {
        isMultiSort = true
        addFirstHeaderRow()
        dataProvider = wrapper
    }

    private val filterMap = mutableMapOf<String, String>()
    private val filterRow = appendHeaderRow()

    fun setColumns(vararg columns: KProperty1<T, Any?>) = setColumns(*columns.map { it.name }.toTypedArray())

    fun setFilterColumns(vararg columns: KProperty1<T, Any?>) = columns.map { getColumnByKey(it.name) }
            .forEach { column ->
                filterRow.getCell(column).setComponent(TextField().apply {
                    placeholder = "Filter..."
                    isClearButtonVisible = true
                    valueChangeMode = ValueChangeMode.LAZY

                    setSizeFull()

                    addValueChangeListener {
                        if (it.value.isEmpty()) {
                            filterMap.remove(column.key)
                        } else {
                            filterMap[column.key] = it.value
                        }
                        wrapper.setFilter(filterMap)
                    }
                })
            }
}