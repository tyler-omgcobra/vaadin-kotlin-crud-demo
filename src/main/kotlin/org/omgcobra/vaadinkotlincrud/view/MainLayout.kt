package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

@Theme(value = Lumo::class)
class MainLayout: AppLayout(), RouterLayout, BeforeEnterObserver {
    private val topBar = Div()

    private var darkMode: Boolean
        get() = Lumo.DARK in element.themeList
        set(value) { element.themeList.set(Lumo.DARK, value) }

    init {
        addToNavbar(topBar)
        darkMode = true
    }

    override fun beforeEnter(event: BeforeEnterEvent?) {
        topBar.removeAll()
        topBar.add(Checkbox("Dark Mode").apply {
            value = darkMode
            addValueChangeListener { darkMode = it.value }
        })
        when (event?.navigationTarget) {
            CrudPage::class.java ->
                topBar.add(Button("Main").apply { addClickListener { UI.getCurrent().navigate(MainPage::class.java) } })
            MainPage::class.java ->
                topBar.add(Button("Grids").apply { addClickListener { UI.getCurrent().navigate(CrudPage::class.java) } })
        }
    }
}