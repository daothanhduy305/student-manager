package com.ebolo.studentmanager.views.utils.ui.tableview

import com.jfoenix.controls.JFXTextField
import javafx.beans.property.ObjectProperty
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder
import javafx.scene.Node
import javafx.scene.control.Cell
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.util.StringConverter
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction2
import kotlin.reflect.jvm.javaMethod

internal object CellUtils {
    private val defaultStringConverter = object : StringConverter<Any>() {
        override fun toString(t: Any?): String? {
            return t?.toString()
        }

        override fun fromString(string: String): Any {
            return string
        }
    }

    // region General convenience

    /**
     * Simple method to provide a StringConverter implementation in various cell
     * implementations.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> defaultStringConverter(): StringConverter<T> {
        return defaultStringConverter as StringConverter<T>
    }

    /**
     * Simple method to provide a TreeItem-specific StringConverter
     * implementation in various cell implementations.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> getItemText(cell: Cell<T>, converter: StringConverter<T>?): String {
        return if (converter == null)
            if (cell.item == null) "" else cell.item.toString()
        else
            converter.toString(cell.item)
    }

    // endregion

    // region TextField convenience

    fun <T> updateItem(
        cell: Cell<T>,
        converter: StringConverter<T>,
        hBox: HBox?,
        graphic: Node?,
        textField: TextField?
    ) {
        if (cell.isEmpty) {
            cell.text = null
            cell.setGraphic(null)
        } else {
            if (cell.isEditing) {
                if (textField != null) {
                    textField.text = getItemText(cell, converter)
                }
                cell.text = null

                if (graphic != null) {
                    hBox!!.children.setAll(graphic, textField)
                    cell.setGraphic(hBox)
                } else {
                    cell.setGraphic(textField)
                }
            } else {
                cell.text = getItemText(cell, converter)
                cell.setGraphic(graphic)
            }
        }
    }

    fun <T> startEdit(
        cell: Cell<T>,
        converter: StringConverter<T>,
        hBox: HBox?,
        graphic: Node?,
        textField: TextField?
    ) {
        if (textField != null) {
            textField.text = getItemText(cell, converter)
        }
        cell.text = null

        if (graphic != null) {
            hBox?.children?.setAll(graphic, textField)
            cell.setGraphic(hBox)
        } else {
            cell.setGraphic(textField)
        }

        textField!!.selectAll()

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        textField.requestFocus()
    }

    fun <T> cancelEdit(cell: Cell<T>, converter: StringConverter<T>, graphic: Node?) {
        cell.text = getItemText(cell, converter)
        cell.graphic = graphic
    }

    fun <T> createTextField(cell: Cell<T>, converter: StringConverter<T>?): JFXTextField {
        val textField = JFXTextField(getItemText(cell, converter))

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        textField.setOnAction { event ->
            if (converter == null) {
                throw IllegalStateException(
                    "Attempting to convert text input into Object, but provided "
                        + "StringConverter is null. Be sure to set a StringConverter "
                        + "in your cell factory.")
            }
            cell.commitEdit(converter.fromString(textField.text))
            event.consume()
        }
        textField.setOnKeyReleased { t ->
            if (t.code == KeyCode.ESCAPE) {
                cell.cancelEdit()
                t.consume()
            }
        }
        return textField
    }

    // endregion
}

/**
 * Convert a pojo bean instance into a writable observable. Support nullable POJO.
 *
 * Example: val observableName = myPojo.observable(MyPojo::getName, MyPojo::setName)
 *            or
 *          val observableName = myPojo.observable(MyPojo::getName)
 *            or
 *          val observableName = myPojo.observable("name")
 */
@Suppress("UNCHECKED_CAST")
fun <S : Any, T : Any?> S.eboloObservable(
    getter: KFunction<T>? = null,
    setter: KFunction2<S, T, Unit>? = null,
    propertyName: String? = null
): ObjectProperty<T> {
    if (getter == null && propertyName == null) throw AssertionError("Either getter or propertyName must be provided")
    val propName = propertyName
        ?: getter?.name?.substring(3)?.decapitalize()

    return JavaBeanObjectPropertyBuilder.create().apply {
        bean(this@eboloObservable)
        this.name(propName)
        if (getter != null) this.getter(getter.javaMethod)
        if (setter != null) this.setter(setter.javaMethod)
    }.build() as ObjectProperty<T>
}