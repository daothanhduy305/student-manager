package com.ebolo.studentmanager.views.teachers

import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTeacherRefreshEvent
import com.ebolo.studentmanager.services.SMTeacherRefreshRequest
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

class SMTeacherTableFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()

    private val teacherList: ObservableList<SMTeacherModel.SMTeacherDto> = FXCollections.observableArrayList()
    private val filteredTeacherList: FilteredList<SMTeacherModel.SMTeacherDto> = FilteredList(teacherList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm giáo viên").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true
                        paddingVertical = 15
                        paddingHorizontal = 30

                        action {
                            find<SMTeacherInfoFragment>(
                                "mode" to SMCRUDUtils.CRUDMode.NEW
                            ).openModal()
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

                            filteredTeacherList.setPredicate { studentDto ->
                                tokens.isEmpty() || tokens.any {
                                    StringUtils.stripAccents(studentDto.firstName).toLowerCase().contains(it)
                                        || StringUtils.stripAccents(studentDto.lastName).toLowerCase().contains(it)
                                }
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMTeacherModel.SMTeacherDto>(filteredTeacherList) {
                multiSelect()

                config.string(
                    "column_orders",
                    "index_column, last_name_column, first_name_column, birthday_column"
                ).split(',').forEach { columnId ->
                    when (columnId.trim()) {
                        "index_column" -> makeIndexColumn("STT").apply {
                            id = "index_column"
                            style {
                                alignment = Pos.TOP_CENTER
                            }
                            setupSizeListeners(config, 100.0)
                        }
                        "last_name_column" -> readonlyColumn("Họ", SMTeacherModel.SMTeacherDto::lastName) {
                            id = "last_name_column"
                            setupSizeListeners(config, 200.0)
                        }
                        "first_name_column" -> readonlyColumn("Tên", SMTeacherModel.SMTeacherDto::firstName) {
                            id = "first_name_column"
                            setupSizeListeners(config, 200.0)
                        }
                        "birthday_column" -> readonlyColumn("Ngày sinh", SMTeacherModel.SMTeacherDto::birthday) {
                            id = "birthday_column"
                            setupSizeListeners(config, 200.0)
                        }
                    }
                }

                columns.onChange { columnsChange ->
                    with(config) {
                        set("column_orders" to columnsChange.list.joinToString { it.id })
                    }
                }

                setOnMouseClicked {
                    if (it.clickCount == 2 && selectionModel.selectedItems.isNotEmpty()) {
                        find<SMTeacherInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "teacherModel" to SMTeacherModel(selectedItem)
                        ).openModal()
                    }
                }

                // set up the context menu
                contextmenu {
                    item("Sửa...").action {
                        find<SMTeacherInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "teacherModel" to SMTeacherModel(selectedItem)
                        ).openModal()
                    }

                    item("Xóa").action {
                        find<SMConfirmDialog>(
                            "dialogContent" to "Tiếp tục xóa?",
                            "onOKClicked" to {
                                val deletingIds = selectionModel.selectedItems.map { it.id }.toList()
                                runAsyncWithOverlay {
                                    serviceCentral.teacherService.deleteTeachers(deletingIds)
                                } ui {
                                    fire(SMTeacherRefreshRequest())
                                }
                            }
                        ).openModal()
                    }
                }

                // subscribe to the refresh event to reset the list
                subscribe<SMTeacherRefreshEvent> { event ->
                    handleItemsUpdated(event.teachers, teacherList)
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMTeacherRefreshRequest())
    }
}