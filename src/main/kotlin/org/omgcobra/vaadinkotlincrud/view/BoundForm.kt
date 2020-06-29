package org.omgcobra.vaadinkotlincrud.view

import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Label
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

        val failure = {
            val oldBean = bean
            bean = referenceBean
            proceed(referenceBean)
            bean = oldBean
        }
        if (referenceBean != bean) {
            Dialog(Label("Save changes to bean?")).apply {
                add(
                        Button("Yes").apply {
                            addClickListener {
                                if (commit()) {
                                    success()
                                } else {
                                    failure()
                                }
                                close()
                            }
                        },
                        Button("No").apply {
                            addClickListener {
                                success()
                                close()
                            }
                        },
                        Button("Cancel").apply {
                            addClickListener {
                                failure()
                                close()
                            }
                        }
                )
            }.open()
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

    fun addCommitListener(listener: (event: CommitEvent<T>) -> Unit): Registration =
            addListener(CommitEvent::class.java as Class<CommitEvent<T>>, listener)
}