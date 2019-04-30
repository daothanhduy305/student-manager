package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMStudentRefreshEvent
import com.ebolo.studentmanager.services.SMStudentRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.stage.Modality
import tornadofx.*

class SMStudentTableView : View() {
    private val serviceCentral: SMServiceCentral by di()

    private val studentList: ObservableList<SMStudentModel.SMStudentDto> = FXCollections.observableArrayList()
    private val filteredStudentList: FilteredList<SMStudentModel.SMStudentDto> = FilteredList(studentList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm học sinh").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true

                        action {
                            find<SMStudentInfoFragment>(
                                "mode" to SMCRUDUtils.CRUDMode.NEW
                            ).openModal(modality = Modality.WINDOW_MODAL, block = true)
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
                            filteredStudentList.setPredicate { studentDto ->
                                studentDto.firstName.toLowerCase().contains(this.text.toLowerCase())
                                    || studentDto.lastName.toLowerCase().contains(this.text.toLowerCase())
                                    || studentDto.nickname.toLowerCase().contains(this.text.toLowerCase())
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMStudentModel.SMStudentDto>(filteredStudentList) {
                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }

                readonlyColumn("Tên", SMStudentModel.SMStudentDto::firstName)
                readonlyColumn("Họ", SMStudentModel.SMStudentDto::lastName)
                readonlyColumn("Nickname", SMStudentModel.SMStudentDto::nickname)
                readonlyColumn("Sinh nhật", SMStudentModel.SMStudentDto::birthday)
                readonlyColumn("Học vấn", SMStudentModel.SMStudentDto::educationLevel) {
                    cellFormat { text = it.title }
                }
                readonlyColumn("Số điện thoại", SMStudentModel.SMStudentDto::phone)

                smartResize()

                // set up the context menu
                contextmenu {
                    item("Sửa...").action {
                        find<SMStudentInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "studentModel" to SMStudentModel(selectedItem))
                            .openModal(modality = Modality.WINDOW_MODAL, block = true)
                    }

                    item("Xóa").action {
                        if (selectedItem != null) runAsync {
                            serviceCentral.studentService.deleteStudent(selectedItem!!.id)
                            fire(SMStudentRefreshRequest)
                        }
                    }
                }

                // subscribe to the refresh event to reset the list
                subscribe<SMStudentRefreshEvent> { event ->
                    runAsync { studentList.setAll(event.students) }.ui {
                        smartResize()
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMStudentRefreshRequest)
    }
}