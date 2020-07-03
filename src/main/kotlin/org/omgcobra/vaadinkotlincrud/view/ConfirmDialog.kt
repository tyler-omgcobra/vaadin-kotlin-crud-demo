package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.theme.lumo.Lumo

class ConfirmDialog(message: String,
                    commit: () -> Boolean = { true },
                    success: () -> Unit? = { },
                    failure: () -> Unit? = { }): Dialog() {

    init {
        isCloseOnEsc = false
        isCloseOnOutsideClick = false

        val yesButton = Button("Yes").apply {
            themeName = "primary"

            addClickListener {
                if (commit()) {
                    success()
                } else {
                    failure()
                }
                close()
            }
        }

        val noButton = Button("No").apply {
            addClickListener {
                success()
                close()
            }
        }

        val cancelButton = Button("Cancel").apply {
            addClickListener {
                failure()
                close()
            }
        }

        add(VerticalLayout().apply {
            alignItems = FlexComponent.Alignment.STRETCH

            add(Label(message), FormLayout(yesButton, noButton, cancelButton).apply {
                setResponsiveSteps(
                        FormLayout.ResponsiveStep("1em", 1),
                        FormLayout.ResponsiveStep("20em", 3)
                )
            })
        })

        width = "50%"
    }

    fun open(dark: Boolean) {
       element.themeList.set(Lumo.DARK, dark)
       open()
    }
}