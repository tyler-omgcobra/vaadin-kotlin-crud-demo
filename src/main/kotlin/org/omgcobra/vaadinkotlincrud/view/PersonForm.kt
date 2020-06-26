package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.data.value.ValueChangeMode
import org.omgcobra.vaadinkotlincrud.db.Person

class PersonForm : BoundForm<Person>(Person::class) {
    private val firstName = TextField("First Name").apply {
        isAutofocus = true
    }
    private val lastName = TextField("Last Name")
    private val street = TextField("Street")
    private val city = TextField("City")
    private val state = TextField("State")
    private val zip = TextField("ZIP")
    private val address = TextField("Email")

    init {
        binder.forMemberField(address).withValidator(EmailValidator("Invalid email address"))

        binder.bindInstanceFields(this)

        content.apply {
            responsiveSteps = listOf(
                    FormLayout.ResponsiveStep("20em", 2),
                    FormLayout.ResponsiveStep("40em", 4),
                    FormLayout.ResponsiveStep("60em", 8),
                    FormLayout.ResponsiveStep("110em", 16))

            add(firstName, 2)
            add(lastName, 2)
            add(address, 4)
            add(street, 4)
            add(city, 2)
            add(state, 1)
            add(zip, 1)
            add(save, 16)
        }

        listOf(firstName, lastName, street, city, state, zip, address).forEach {
            it.apply {
                valueChangeMode = ValueChangeMode.EAGER
                addKeyPressListener(Key.ENTER, ComponentEventListener { commit() })
                setSizeFull()
            }
        }
    }
}