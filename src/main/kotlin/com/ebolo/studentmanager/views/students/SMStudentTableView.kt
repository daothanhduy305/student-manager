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
import org.apache.commons.lang3.StringUtils
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
                        paddingVertical = 15
                        paddingHorizontal = 30

                        action {
                            find<SMStudentInfoFragment>(
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

                            filteredStudentList.setPredicate { studentDto ->
                                tokens.isEmpty() || tokens.any {
                                    StringUtils.stripAccents(studentDto.firstName).toLowerCase().contains(it)
                                        || StringUtils.stripAccents(studentDto.lastName).toLowerCase().contains(it)
                                        || StringUtils.stripAccents(studentDto.nickname).toLowerCase().contains(it)
                                }
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMStudentModel.SMStudentDto>(filteredStudentList) {
                multiSelect()

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
                            .openModal()
                    }

                    item("Xóa").action {
                        serviceCentral.studentService.deleteStudents(selectionModel.selectedItems.map { it.id }.toList())
                        fire(SMStudentRefreshRequest)
                    }
                }

                // subscribe to the refresh event to reset the list
                subscribe<SMStudentRefreshEvent> { event ->
                    runAsync { studentList.setAll(event.students) } ui { requestResize() }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMStudentRefreshRequest)
    }
}