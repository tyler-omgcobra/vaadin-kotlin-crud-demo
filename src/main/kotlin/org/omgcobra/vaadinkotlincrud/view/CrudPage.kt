package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.omgcobra.vaadinkotlincrud.db.*
import kotlin.reflect.full.createInstance

@PageTitle("Form")
@Route("form", layout = GridLayout::class)
class CrudPage(personDAO: PersonDAO): Composite<VerticalLayout>() {
    private val grid: FilteredGrid<Person> = FilteredGrid(personDAO, Person::class).apply {
        setColumns(Person::id, Person::firstName, Person::lastName, Person::address, Person::street)
        setFilterColumns(Person::firstName, Person::lastName, Person::address, Person::street)

        addSelectionListener { event ->
            form.setBean(event.firstSelectedItem.orElseGet(Person::class::createInstance)) { select(it) }
        }
    }

    private val form: PersonForm = PersonForm().apply {
        addCommitListener {
            bean = personDAO.save(it.bean)
            referenceBean = bean
            grid.dataProvider.refreshAll()
            grid.select(bean)
        }
    }

    init {
        content.apply {
            alignItems = FlexComponent.Alignment.STRETCH
            setSizeFull()
            add(grid, form)
        }
    }
}