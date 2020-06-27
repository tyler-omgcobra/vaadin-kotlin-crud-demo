package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.shared.Registration
import org.omgcobra.vaadinkotlincrud.db.Entity
import org.omgcobra.vaadinkotlincrud.db.create
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


open class BoundForm<T : Entity>(private val typeClass: KClass<T>) : Composite<FormLayout>() {
    class CommitEvent<B : Entity>(source: BoundForm<B>, val bean: B) : ComponentEvent<BoundForm<B>>(source, false)

    protected val save = Button("Save").apply {
        addClickListener { commit() }
    }

    protected val binder = Binder(typeClass.java).apply {
        addStatusChangeListener { this@BoundForm.save.isEnabled = !it.hasValidationErrors() }
        bean = typeClass.createInstance()
    }

    var bean: T
        get() = binder.bean
        set(value) {
            binder.bean = typeClass.create { it.getter.call(value) }
        }

    protected fun commit() {
        if (binder.validate().isOk) {
            fireEvent(CommitEvent(source = this, bean = bean))
        }
    }

    fun addCommitListener(listener: (event: CommitEvent<T>) -> Unit): Registration =
            addListener(CommitEvent::class.java as Class<CommitEvent<T>>, listener)
}