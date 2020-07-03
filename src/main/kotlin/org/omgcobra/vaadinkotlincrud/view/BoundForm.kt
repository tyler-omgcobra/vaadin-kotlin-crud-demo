package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.router.BeforeLeaveEvent
import com.vaadin.flow.router.BeforeLeaveObserver
import com.vaadin.flow.shared.Registration
import com.vaadin.flow.theme.lumo.Lumo
import org.omgcobra.vaadinkotlincrud.db.Entity
import org.omgcobra.vaadinkotlincrud.db.create
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


open class BoundForm<T : Entity>(private val typeClass: KClass<T>) : FormLayout(), BeforeLeaveObserver {
    class CommitEvent<B : Entity>(source: BoundForm<B>, val bean: B) : ComponentEvent<BoundForm<B>>(source, false)

    protected val save = Button("Save").apply {
        themeName = "primary"
        addClickListener { commit() }
    }

    protected val binder = Binder(typeClass.java).apply {
        addStatusChangeListener { this@BoundForm.save.isEnabled = !it.hasValidationErrors() }
        bean = typeClass.createInstance()
        referenceBean = bean
    }

    var referenceBean: T
        set(value) {
            field = typeClass.create { it.getter.call(value) }
        }

    var bean: T
        get() = binder.bean
        set(value) {
            binder.bean = typeClass.create { it.getter.call(value) }
        }

    fun setBean(value: T, proceed: (T) -> Unit) {
        val success = {
            bean = value
            referenceBean = value
            proceed(value)
        }

        if (referenceBean != bean) {
            val failure: () -> Unit = {
                val oldBean = bean
                bean = referenceBean
                proceed(referenceBean)
                bean = oldBean
                binder.validate()
            }

            val dark = UI.getCurrent().element.getChild(0).themeList.contains(Lumo.DARK)
            ConfirmDialog("Save changes to bean?", this::commit, success, failure).open(dark)
        } else {
            success()
        }
    }

    fun commit(): Boolean {
        if (binder.validate().isOk) {
            fireEvent(CommitEvent(source = this, bean = bean))
            return true
        }

        return false
    }

    @Suppress("UNCHECKED_CAST")
    fun addCommitListener(listener: (event: CommitEvent<T>) -> Unit): Registration =
            addListener(CommitEvent::class.java as Class<CommitEvent<T>>, listener)

    override fun beforeLeave(event: BeforeLeaveEvent?) {
        event?.let {
            val success = it.postpone()::proceed

            if (referenceBean != bean) {
                val dark = UI.getCurrent().element.getChild(0).themeList.contains(Lumo.DARK)
                ConfirmDialog(message = "Save changes to bean?", commit = this::commit, success = success).open(dark)
            } else {
                success()
            }
        }
    }
}