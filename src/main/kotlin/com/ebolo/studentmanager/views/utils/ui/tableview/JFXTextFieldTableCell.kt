package com.ebolo.studentmanager.views.utils.ui.tableview

import com.jfoenix.controls.JFXTextField
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.util.Callback
import javafx.util.StringConverter

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
    private var textField: JFXTextField? = null

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