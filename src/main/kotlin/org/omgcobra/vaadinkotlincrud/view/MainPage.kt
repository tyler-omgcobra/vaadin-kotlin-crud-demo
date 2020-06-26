package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.router.Route

@Route("", layout = MainLayout::class)
class MainPage: Composite<Div>() {
    private val label = Label("hello world")
    private val button = Button("click me").apply {
        addClickListener { clickButton() }
    }
    private val go = Button("go").apply {
        addClickListener { UI.getCurrent().navigate(CrudPage::class.java) }
    }

    init {
        content.apply {
            add(
                    label.apply {
                        setId("helloLabel")
                        style["font-weight"] = "bold"
                    },
                    button.apply {
                        setId("clickMeButton")
                    },
                    go.apply {
                        setId("goButton")
                    }
            )

            setSizeFull()
            style.apply {
                set("background", "grey")
                set("display", "flex")
                set("flex-direction", "column")
            }
        }
    }

    private fun clickButton() {
        label.text = "clicked it"
    }
}