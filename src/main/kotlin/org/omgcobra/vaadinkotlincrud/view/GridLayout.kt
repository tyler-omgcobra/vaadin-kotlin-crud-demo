package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.ParentLayout
import com.vaadin.flow.router.RouterLayout

@ParentLayout(MainLayout::class)
class GridLayout: VerticalLayout(), RouterLayout {
    init {
        setSizeFull()

        style["align-items"] = "center"

        add(H3("Grid"))
    }
}