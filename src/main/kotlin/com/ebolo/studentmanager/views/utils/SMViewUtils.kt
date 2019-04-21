package com.ebolo.studentmanager.views.utils

import com.jfoenix.controls.JFXTextField
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.util.Callback
import javafx.util.StringConverter

class SMViewUtils {

    open class JFXTextFieldTableCell<S, T>
    /**
     * Creates a TextFieldTableCell that provides a [TextField] when put
     * into editing mode that allows editing of the cell content. This method
     * will work on any TableColumn instance, regardless of its generic type.
     * However, to enable this, a [StringConverter] must be provided that
     * will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * [TableColumn.onEditCommitProperty] callback.
     *
     * @param converter A [converter][StringConverter] that can convert
     * the given String (from what the user typed in) into an instance of
     * type T.
     */
    @JvmOverloads constructor(converter: StringConverter<T>? = null) : TableCell<S, T>() {

        /***************************************************************************
         * *
         * Fields                                                                  *
         * *
         */

        private var textField: JFXTextField? = null


        /***************************************************************************
         * *
         * Properties                                                              *
         * *
         */

        // --- converter
        private val converter = SimpleObjectProperty<StringConverter<T>>(this, "converter")

        init {
            this.styleClass.add("text-field-table-cell")
            setConverter(converter)
        }

        /**
         * The [StringConverter] property.
         */
        private fun converterProperty(): ObjectProperty<StringConverter<T>> {
            return converter
        }

        /**
         * Sets the [StringConverter] to be used in this cell.
         */
        private fun setConverter(value: StringConverter<T>?) {
            converterProperty().set(value)
        }

        /**
         * Returns the [StringConverter] used in this cell.
         */
        private fun getConverter(): StringConverter<T> {
            return converterProperty().get()
        }


        /***************************************************************************
         * *
         * Public API                                                              *
         * *
         */

        /** {@inheritDoc}  */
        override fun startEdit() {
            if (!isEditable
                || !tableView.isEditable
                || !tableColumn.isEditable) {
                return
            }
            super.startEdit()

            if (isEditing) {
                if (textField == null) {
                    textField = CellUtils.createTextField(this, getConverter())
                }

                CellUtils.startEdit(this, getConverter(), null, null, textField!!)
            }
        }

        /** {@inheritDoc}  */
        override fun cancelEdit() {
            super.cancelEdit()
            CellUtils.cancelEdit(this, getConverter(), null)
        }

        /** {@inheritDoc}  */
        public override fun updateItem(item: T, empty: Boolean) {
            super.updateItem(item, empty)
            CellUtils.updateItem(this, getConverter(), null, null, textField)
        }

        companion object {

            /***************************************************************************
             * *
             * Static cell factories                                                   *
             * *
             */

            /**
             * Provides a [TextField] that allows editing of the cell content when
             * the cell is double-clicked, or when
             * [TableView.edit] is called.
             * This method will work  on any [TableColumn] instance, regardless of
             * its generic type. However, to enable this, a [StringConverter] must
             * be provided that will convert the given String (from what the user typed
             * in) into an instance of type T. This item will then be passed along to the
             * [TableColumn.onEditCommitProperty] callback.
             *
             * @param converter A [StringConverter] that can convert the given String
             * (from what the user typed in) into an instance of type T.
             * @return A [Callback] that can be inserted into the
             * [cell factory property][TableColumn.cellFactoryProperty] of a
             * TableColumn, that enables textual editing of the content.
             */
            fun <S, T> forTableColumn(
                converter: StringConverter<T>
            ): Callback<TableColumn<S, T>, TableCell<S, T>> {
                return object : Callback<TableColumn<S, T>, TableCell<S, T>> {
                    override fun call(param: TableColumn<S, T>?): TableCell<S, T> {
                        return object : JFXTextFieldTableCell<S, T>(converter) {}
                    }
                }
            }
        }
    }

    internal object CellUtils {
        /***************************************************************************
         * *
         * General convenience                                                     *
         * *
         */

        private fun <T> getItemText(cell: Cell<T>, converter: StringConverter<T>?): String {
            return if (converter == null)
                if (cell.item == null) "" else cell.item.toString()
            else
                converter.toString(cell.item)
        }


        /***************************************************************************
         * *
         * TextField convenience                                                   *
         * *
         */
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
    }

    class SMGradeConverter : StringConverter<Int>() {
        /** {@inheritDoc}  */
        override fun fromString(value: String?): Int? {
            if (value == null) return null
            // If the specified value is null or zero-length, return null

            val trimmedValue = value.trim { it <= ' ' }

            return if (trimmedValue.isEmpty()) {
                -1
            } else Integer.valueOf(trimmedValue)

        }

        /** {@inheritDoc}  */
        override fun toString(value: Int?): String {
            // If the specified value is null, return a zero-length String
            return if (value == null || value < 0) {
                ""
            } else Integer.toString(value.toInt())

        }
    }
}