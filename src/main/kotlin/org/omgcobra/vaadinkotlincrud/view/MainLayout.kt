package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

@Theme(value = Lumo::class)
class MainLayout: AppLayout(), RouterLayout, BeforeEnterObserver {
    private val topBar = HorizontalLayout().apply {
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
    }

    private var darkMode: Boolean
        get() = Lumo.DARK in element.themeList
        set(value) { element.themeList.set(Lumo.DARK, value) }

    init {
        addToNavbar(topBar)
        darkMode = true
    }

    override fun beforeEnter(event: BeforeEnterEvent?) {
        topBar.removeAll()

        listOf(
                "Main" to MainPage::class,
                "Form" to CrudPage::class,
                "Grid" to BasicViewPage::class
        ).forEach { pair ->
            if (event?.navigationTarget?.equals(pair.second.java) != true) {
                topBar.add(Button(pair.first).apply {
                    addClickListener { UI.getCurrent().navigate(pair.second.java) }
                })
            }
        }

        topBar.addAndExpand(Checkbox("Dark Mode").apply {
            style["text-align"] = "right"
            value = darkMode
            addValueChangeListener { darkMode = it.value }
        })
    }
}