package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route("", layout = MainLayout::class)
class MainPage: Composite<VerticalLayout>() {
    private val label = Label("hello world").apply {
        style["font-weight"] = "bold"
    }

    private val button = Button("click me").apply {
        addClickListener { clickButton() }
    }

    private val go = Button("go").apply {
        addClickListener { UI.getCurrent().navigate(CrudPage::class.java) }
    }

    init {
        content.apply {
            alignItems = FlexComponent.Alignment.STRETCH
            setSizeFull()

            add(label, button, go)
        }
    }

    private fun clickButton() {
        label.text = "clicked it"
    }
}