package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import org.omgcobra.vaadinkotlincrud.db.*
import kotlin.reflect.full.createInstance

@Route("form", layout = GridLayout::class)
class CrudPage(personDAO: PersonDAO): Composite<VerticalLayout>() {
    private val grid: FilteredGrid<Person> = FilteredGrid(personDAO, Person::class).apply {
        setColumns(Person::id, Person::firstName, Person::lastName, Person::address, Person::street)
        setFilterColumns(Person::firstName, Person::lastName, Person::address, Person::street)

        dataProvider.addDataProviderListener { select(form.bean) }
        addSelectionListener { form.bean = it.firstSelectedItem.orElseGet(Person::class::createInstance) }
    }

    private val form: PersonForm = PersonForm().apply {
        addCommitListener {
            bean = personDAO.save(it.bean)
            grid.dataProvider.refreshItem(it.bean)
        }
    }

    init {
        content.apply {
            setSizeFull()
            add(grid, form)
        }
    }
}