package com.ebolo.studentmanager.views.teachers

import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTeacherRefreshEvent
import com.ebolo.studentmanager.services.SMTeacherRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class SMTeacherTableView : View() {
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
                            filteredTeacherList.setPredicate { studentDto ->
                                studentDto.firstName.toLowerCase().contains(this.text.toLowerCase())
                                    || studentDto.lastName.toLowerCase().contains(this.text.toLowerCase())
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMTeacherModel.SMTeacherDto>(filteredTeacherList) {
                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }

                readonlyColumn("Họ", SMTeacherModel.SMTeacherDto::lastName)
                readonlyColumn("Tên", SMTeacherModel.SMTeacherDto::firstName)
                readonlyColumn("Ngày sinh", SMTeacherModel.SMTeacherDto::birthday)

                smartResize()

                // set up the context menu
                contextmenu {
                    item("Sửa...").action {
                        find<SMTeacherInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "teacherModel" to SMTeacherModel(selectedItem)
                        ).openModal()
                    }

                    item("Xóa").action {
                        if (selectedItem != null) runAsync {
                            serviceCentral.teacherService.deleteTeacher(selectedItem!!.id)
                            fire(SMTeacherRefreshRequest)
                        }
                    }
                }

                // subscribe to the refresh event to reset the list
                subscribe<SMTeacherRefreshEvent> { event ->
                    runAsync { teacherList.setAll(event.teachers) }.ui {
                        smartResize()
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMTeacherRefreshRequest)
    }
}