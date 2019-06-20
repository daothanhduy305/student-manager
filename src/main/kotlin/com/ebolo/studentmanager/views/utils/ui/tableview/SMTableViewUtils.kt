package com.ebolo.studentmanager.views.utils.ui.tableview

import com.ebolo.studentmanager.models.SMBaseModel
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import tornadofx.*

fun <S : SMBaseModel.SMBaseDto> (TableView<S>).handleItemsUpdated(newItems: List<S>, mainItemsList: ObservableList<S>) {
    val selectingItems = selectionModel.selectedItems.map { it.id }
    runAsync {
        mainItemsList.setAll(newItems)
    } ui { success ->
        if (success && selectingItems.isNotEmpty()) {
            runAsync {
                val indices = mutableListOf<Int>()
                items.forEachIndexed { index, dto ->
                    if (dto.id in selectingItems) {
                        indices.add(index)
                    }
                }
                indices
            } ui {
                it.forEach { index -> selectionModel.select(index) }
            }

        }
    }
}

fun <S : SMBaseModel.SMBaseDto, T : Any> (TableColumn<S, T>).setupSizeListeners(
    config: ConfigProperties,
    defaultValue: Double
) {
    widthProperty().onChange { newWidth: Double ->
        with(config) {
            set(id to newWidth)
            save()
        }
    }

    this.prefWidth(config.double(id, defaultValue))
}