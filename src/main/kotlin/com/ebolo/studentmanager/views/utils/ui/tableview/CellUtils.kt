package com.ebolo.studentmanager.views.utils.ui.tableview

import com.jfoenix.controls.JFXTextField
import javafx.scene.Node
import javafx.scene.control.Cell
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.util.StringConverter

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