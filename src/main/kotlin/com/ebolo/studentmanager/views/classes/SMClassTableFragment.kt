package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.services.SMClassListRefreshEvent
import com.ebolo.studentmanager.services.SMClassListRefreshRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.ebolo.studentmanager.views.utils.ui.SMConfirmDialog
import com.ebolo.studentmanager.views.utils.ui.tableview.handleItemsUpdated
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.apache.commons.lang3.StringUtils
import tornadofx.*

class SMClassTableFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()

    private val classList: ObservableList<SMClassModel.SMClassDto> = FXCollections.observableArrayList()
    private val filteredClassList: FilteredList<SMClassModel.SMClassDto> = FilteredList(classList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm lớp").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true
                        paddingVertical = 15
                        paddingHorizontal = 30

                        action {
                            find<SMClassInfoFragment>(
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

                            filteredClassList.setPredicate { classDto ->
                                tokens.isEmpty() || tokens.any {
                                    StringUtils.stripAccents(classDto.name).toLowerCase().contains(it)
                                        || StringUtils.stripAccents(classDto.subject.name).toLowerCase().contains(it)
                                        || StringUtils.stripAccents(classDto.teacher.firstName).toLowerCase().contains(it)
                                        || StringUtils.stripAccents(classDto.teacher.lastName).toLowerCase().contains(it)
                                }
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMClassModel.SMClassDto>(filteredClassList) {
                multiSelect()

                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }.prefWidth(100.0)

                readonlyColumn("Tên lớp", SMClassModel.SMClassDto::name).prefWidth(200.0)

                readonlyColumn("Giáo viên", SMClassModel.SMClassDto::teacher) {
                    cellFormat { teacher -> text = "${teacher.lastName} ${teacher.firstName}" }
                }.prefWidth(200.0)

                readonlyColumn("Môn", SMClassModel.SMClassDto::subject) {
                    cellFormat { subject -> text = subject.name }
                }.prefWidth(200.0)

                // set up the context menu
                contextmenu {
                    item("Sửa...").action {
                        find<SMClassInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "classModel" to SMClassModel(selectedItem)
                        ).openModal()
                    }

                    item("Xóa").action {
                        find<SMConfirmDialog>(
                            "dialogContent" to "Tiếp tục xóa?",
                            "onOKClicked" to {
                                val selectingIds = selectionModel.selectedItems.map { it.id }.toList()
                                runAsyncWithOverlay {
                                    serviceCentral.classService.deleteClasses(selectingIds)
                                } ui {
                                    fire(SMClassListRefreshRequest())
                                }
                            }
                        ).openModal()
                    }
                }

                setOnMouseClicked {
                    if (it.clickCount == 2 && selectionModel.selectedItems.isNotEmpty()) {
                        find<SMClassInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "classModel" to SMClassModel(selectedItem)
                        ).openModal()
                    }
                }

                // subscribe to the refresh event to reset the list
                subscribe<SMClassListRefreshEvent> { event ->
                    handleItemsUpdated(event.classes, classList)
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMClassListRefreshRequest())
    }
}