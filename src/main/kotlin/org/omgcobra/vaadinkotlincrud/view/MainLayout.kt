package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

@Theme(value = Lumo::class)
class MainLayout: AppLayout(), RouterLayout, BeforeEnterObserver {
    private val buttonMap: Map<Class<out Component>, Button>

    private var darkMode: Boolean
        get() = Lumo.DARK in element.themeList
        set(value) { element.themeList.set(Lumo.DARK, value) }

    init {
        darkMode = true

        addToNavbar(HorizontalLayout().apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

            buttonMap = mapOf(*UI.getCurrent().router.registry.registeredRoutes.map { it.navigationTarget }.map { page ->
                val button = Button(page.getAnnotation(PageTitle::class.java).value).apply {
                    addClickListener { UI.getCurrent().navigate(page) }
                }
                add(button)
                page to button
            }.toTypedArray())

            addAndExpand(Checkbox("Dark Mode").apply {
                style["text-align"] = "right"
                value = darkMode
                addValueChangeListener { darkMode = it.value }
            })
        })
    }

    override fun beforeEnter(event: BeforeEnterEvent?) {
        buttonMap.forEach { (page, button) ->
            button.isEnabled = page != event?.navigationTarget
        }
    }
}