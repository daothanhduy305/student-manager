package com.ebolo.studentmanager.views.subjects

import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMSubjectRefreshEvent
import com.ebolo.studentmanager.services.SMSubjectRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.ebolo.studentmanager.views.utils.ui.SMConfirmDialog
import com.ebolo.studentmanager.views.utils.ui.tableview.handleItemsUpdated
import com.ebolo.studentmanager.views.utils.ui.tableview.setupSizeListeners
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.apache.commons.lang3.StringUtils
import tornadofx.*

class SMSubjectTableFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()

    private val subjectList: ObservableList<SMSubjectModel.SMSubjectDto> = FXCollections.observableArrayList()
    private val filteredSubjectList: FilteredList<SMSubjectModel.SMSubjectDto> = FilteredList(subjectList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm môn học").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true
                        paddingVertical = 15
                        paddingHorizontal = 30

                        action {
                            find<SMSubjectInfoFragment>().openModal()
                        }

                        style {
                            backgroundColor += c("#ffffff")
                        }
                    }
                }

                // Search box and misc
                hbox {
                    alignment = Pos.CENTER_RIGHT
                    hgrow = Priority.ALWAYS

                    searchBox = JFXTextField().apply {
                        promptText = "Tìm kiếm"

                        textProperty().addListener { _, _, _ ->
                            val tokens = this.text
                                .split(' ')
                                .filter { it.isNotBlank() }
                                .map { StringUtils.stripAccents(it).toLowerCase() }

                            filteredSubjectList.setPredicate { subjectDto ->
                                tokens.isEmpty() || tokens.any {
                                    StringUtils.stripAccents(subjectDto.name).toLowerCase().contains(it)
                                }
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMSubjectModel.SMSubjectDto>(filteredSubjectList) {
                multiSelect()

                config.string(
                    "column_orders",
                    "index_column, subject_column"
                ).split(',').forEach { columnId ->
                    when (columnId.trim()) {
                        "index_column" -> makeIndexColumn("STT").apply {
                            id = "index_column"
                            style {
                                alignment = Pos.TOP_CENTER
                            }

                            setupSizeListeners(config, 100.0)
                        }
                        "subject_column" -> readonlyColumn("Tên môn học", SMSubjectModel.SMSubjectDto::name) {
                            id = "subject_column"
                            setupSizeListeners(config, 200.0)
                        }
                    }

                }

                columns.onChange { columnsChange ->
                    with(config) {
                        set("column_orders" to columnsChange.list.joinToString { it.id })
                    }
                }

                contextmenu {
                    item("Sửa...").action {
                        find<SMSubjectInfoFragment>(
                            "subjectModel" to SMSubjectModel(selectedItem),
                            "mode" to SMCRUDUtils.CRUDMode.EDIT
                        ).openModal()
                    }

                    item("Xóa").action {
                        find<SMConfirmDialog>(
                            "dialogContent" to "Tiếp tục xóa?",
                            "onOKClicked" to {
                                val deletingIds = selectionModel.selectedItems.map { it.id }.toList()

                                runAsyncWithOverlay {
                                    serviceCentral.subjectService.deleteSubjects(deletingIds)
                                } ui {
                                    fire(SMSubjectRefreshRequest())
                                }
                            }
                        ).openModal()
                    }
                }

                setOnMouseClicked {
                    if (it.clickCount == 2 && selectionModel.selectedItems.isNotEmpty()) {
                        find<SMSubjectInfoFragment>(
                            "subjectModel" to SMSubjectModel(selectedItem),
                            "mode" to SMCRUDUtils.CRUDMode.EDIT
                        ).openModal()
                    }
                }

                subscribe<SMSubjectRefreshEvent> { event ->
                    handleItemsUpdated(event.subjects, subjectList)
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMSubjectRefreshRequest())
    }
}