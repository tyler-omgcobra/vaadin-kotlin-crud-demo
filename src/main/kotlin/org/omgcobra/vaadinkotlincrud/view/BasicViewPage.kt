package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.omgcobra.vaadinkotlincrud.db.*

@PageTitle("Grid")
@Route("grid", layout = MainLayout::class)
class BasicViewPage(valueDAO: ValueDAO): VerticalLayout() {
    private val grid: FilteredGrid<Basic> = FilteredGrid(valueDAO, Basic::class).apply {
        setColumns(
                Basic::differentNamedId,
                Basic::value,
                Basic::truthy,
                Basic::floaty
        )

        setFilterColumns(
                Basic::differentNamedId,
                Basic::value,
                Basic::truthy,
                Basic::floaty
        )
    }

    init {
        alignItems = FlexComponent.Alignment.STRETCH
        setSizeFull()
        add(grid)
    }
}