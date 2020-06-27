package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import org.omgcobra.vaadinkotlincrud.db.*
import kotlin.reflect.full.createInstance

@Route("form", layout = MainLayout::class)
class CrudPage(personDAO: PersonDAO): Composite<Div>() {
    private val grid = FilteredGrid(personDAO, Person::class).apply {
        setColumns(Person::id, Person::firstName, Person::lastName, Person::address, Person::street)
        setFilterColumns(Person::firstName, Person::lastName, Person::address)
        style["margin"] = "1em 5%"
    }

    private val form = PersonForm()

    init {
        content.apply {
            add(
                    grid,
                    form
            )
            style.apply {
                set("display", "flex")
                set("flex-direction", "column")
            }
        }

        grid.dataProvider.addDataProviderListener {
            grid.select(form.binder.bean)
        }

        grid.addSelectionListener {
            form.binder.bean = it.firstSelectedItem.orElseGet(Person::class::createInstance)
        }

        form.addCommitListener {
            form.binder.bean = personDAO.save(it.bean)
            grid.dataProvider.refreshItem(it.bean)
        }
    }
}