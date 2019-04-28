package com.ebolo.studentmanager.views.utils.ui.tableview

import com.jfoenix.controls.JFXCheckBox
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

/**
 * A class containing a [TableCell] implementation that draws a
 * [CheckBox] node inside the cell, optionally with a label to indicate
 * what the checkbox represents.
 *
 *
 * By default, the CheckBoxTableCell is rendered with a CheckBox centred in
 * the TableColumn. If a label is required, it is necessary to provide a
 * non-null StringConverter instance to the
 * [.CheckBoxTableCell] constructor.
 *
 *
 * To construct an instance of this class, it is necessary to provide a
 * [Callback] that, given an object of type T, will return an
 * `ObservableProperty<Boolean>` that represents whether the given item is
 * selected or not. This ObservableValue will be bound bidirectionally (meaning
 * that the CheckBox in the cell will set/unset this property based on user
 * interactions, and the CheckBox will reflect the state of the ObservableValue,
 * if it changes externally).
 *
 *
 * Note that the CheckBoxTableCell renders the CheckBox 'live', meaning that
 * the CheckBox is always interactive and can be directly toggled by the user.
 * This means that it is not necessary that the cell enter its
 * [editing state][.editingProperty] (usually by the user double-clicking
 * on the cell). A side-effect of this is that the usual editing callbacks
 * (such as [on edit commit][javafx.scene.control.TableColumn.onEditCommitProperty])
 * will **not** be called. If you want to be notified of changes,
 * it is recommended to directly observe the boolean properties that are
 * manipulated by the CheckBox.
 *
 * @param <T> The type of the elements contained within the TableColumn.
 * @since JavaFX 2.2
</T> */
open class JFXCheckboxTableCell<S>(
    onValueChanged: ((item: S?, value: Boolean) -> Unit)? = null
) : TableCell<S, Boolean>() {
    private val checkBox: JFXCheckBox
    private val onValueChanged: ((item: S?, value: Boolean) -> Unit)?

    init {
        // we let getSelectedProperty be null here, as we can always defer to the
        // TableColumn
        this.styleClass.add("check-box-table-cell")

        // setup the callback to be called on checkbox action invoked
        this.checkBox = JFXCheckBox().apply {
            setOnAction {
                @Suppress("UNCHECKED_CAST")
                onValueChanged?.invoke(tableRow?.item as S?, this.isSelected)
            }
        }

        // by default the graphic is null until the cell stops being empty
        graphic = null

        this.onValueChanged = onValueChanged
    }

    /** {@inheritDoc}  */
    override fun updateItem(item: Boolean?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            text = null
            graphic = null
        } else {
            graphic = checkBox.apply {
                isSelected = item!!
            }
        }
    }

    companion object {
        /**
         * Creates a cell factory for use in a [TableColumn] cell factory.
         * This method requires that the TableColumn be of type
         * `ObservableValue<Boolean>`.
         *
         *
         * When used in a TableColumn, the CheckBoxCell is rendered with a
         * CheckBox centered in the column.
         *
         * @param <T> The type of the elements contained within the [TableColumn]
         * instance.
         * @return A [Callback] that will return a [TableCell] that is
         * able to work on the type of element contained within the TableColumn.
        </T> */
        fun <S> forTableColumn(
            onValueChanged: ((item: S?, value: Boolean) -> Unit)? = null
        ): Callback<TableColumn<S, Boolean>, TableCell<S, Boolean>> {
            return Callback { object : JFXCheckboxTableCell<S>(onValueChanged) {} }
        }
    }
}