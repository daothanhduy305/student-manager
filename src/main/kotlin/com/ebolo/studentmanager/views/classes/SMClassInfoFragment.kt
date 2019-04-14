package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassRefreshRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXAutoCompletePopup
import com.jfoenix.controls.JFXListCell
import com.jfoenix.controls.JFXTextField
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.control.ButtonType
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*


class SMClassInfoFragment : Fragment("Thông tin lớp học") {
    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param()
    private val classModel: SMClassModel by param(SMClassModel())

    private val subjectList by lazy { FXCollections.observableList(serviceCentral.subjectService.getSubjects()) }
    private val teacherList by lazy { FXCollections.observableList(serviceCentral.teacherService.getTeacherList()) }
    private val studentList by lazy { FXCollections.observableList(serviceCentral.studentService.getStudentList()) }

    override val root = tabpane {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        tab("Thông tin lớp") {
            form {
                hbox {
                    vbox {
                        fieldset(labelPosition = Orientation.HORIZONTAL) {
                            field("Tên lớp") {
                                textfield(classModel.name).required()
                            }

                            field("Môn học") {
                                combobox(classModel.subject, values = subjectList) {
                                    if (mode != SMCRUDUtils.CRUDMode.NEW) {
                                        value = subjectList.first { it.id == classModel.item.subject.id }
                                    }
                                    cellFormat { subject -> text = subject.name }
                                    vgrow = Priority.ALWAYS
                                    useMaxWidth = true
                                }.required()
                            }

                            field("Giáo viên") {
                                combobox(classModel.teacher, values = teacherList) {
                                    if (mode != SMCRUDUtils.CRUDMode.NEW) {
                                        value = teacherList.first { it.id == classModel.item.teacher.id }
                                    }
                                    cellFormat { teacher ->
                                        text = "${teacher.lastName} ${teacher.firstName}"
                                    }
                                    vgrow = Priority.ALWAYS
                                    useMaxWidth = true
                                }.required()
                            }

                            field("Ngày bắt đầu") {
                                datepicker(classModel.startDate).required()
                            }

                            field("Học phí") {
                                textfield(classModel.tuitionFee) {
                                    validator { text ->
                                        when {
                                            text.isNullOrBlank() -> error("This field is required")
                                            !text.isInt() -> error("Number is required")
                                            else -> null
                                        }
                                    }
                                }
                            }

                            field("Số cột điểm") {
                                textfield(classModel.numberOfExams).required()
                            }
                        }
                    }

                    vbox {
                        paddingLeft = 15.0
                        spacing = 10.0

                        button("Hoàn tất") {
                            vgrow = Priority.ALWAYS
                            useMaxWidth = true

                            enableWhen(Bindings.and(classModel.dirty, classModel.valid))

                            action {
                                // base on the crud mode, we define the appropriate action
                                val result: SMCRUDUtils.SMCRUDResult = when (mode) {
                                    SMCRUDUtils.CRUDMode.NEW -> serviceCentral.classService.createNewClass(classModel)
                                    SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.classService.editClass(classModel)
                                    else -> {
                                        error("Đã xảy ra lỗi", "Unsupported CRUD mode", ButtonType.CLOSE)
                                        SMCRUDUtils.SMCRUDResult(false)
                                    }
                                }
                                // refresh if success
                                if (result.success) {
                                    fire(SMClassRefreshRequest)
                                    modalStage?.close()
                                } else {
                                    error("Đã xảy ra lỗi", result.errorMessage, ButtonType.CLOSE)
                                }
                            }
                        }

                        button("Hủy bỏ") {
                            vgrow = Priority.ALWAYS
                            useMaxWidth = true

                            action { modalStage?.close() }
                        }
                    }
                }
            }
        }

        tab("Danh sách học viên") {
            borderpane {
                center = find<SMClassStudentListFragment>(
                    "classModel" to classModel
                ).root

                // Top is a search box to search for the student to add into the class
                top = form {
                    fieldset(labelPosition = Orientation.VERTICAL) {
                        field("Thêm học viên") {
                            // Text field to search for the student
                            this += JFXTextField().apply studentSearchTextField@{
                                val autoCompletePopup = JFXAutoCompletePopup<SMStudentModel.SMStudentDto>().apply {
                                    fixedCellSize = 36.0

                                    suggestions.addAll(studentList)
                                    setSelectionHandler { event ->
                                        // When select, set the text field to be empty
                                        // add the student into the student list of the class
                                        val chosenStudent = event.getObject()
                                        this@studentSearchTextField.text = ""
                                        classModel.item.studentList.add(chosenStudent)
                                    }

                                    // Make the cells in the auto-complete popup to show the student's name
                                    setSuggestionsCellFactory {
                                        object : JFXListCell<SMStudentModel.SMStudentDto>() {
                                            override fun updateItem(item: SMStudentModel.SMStudentDto?, empty: Boolean) {
                                                super.updateItem(item, empty)

                                                if (!empty) {
                                                    text = "${item!!.lastName} ${item.firstName}"
                                                }
                                            }
                                        }
                                    }
                                }

                                // Filter out the students in the return list
                                textProperty().addListener { _, _, _ ->
                                    autoCompletePopup.filter { chosenStudent ->
                                        val currentValue = this.text.toLowerCase()
                                        !classModel.item.studentList.any { student ->
                                            student.id == chosenStudent.id
                                        } // This student has not already been in the class
                                            && (chosenStudent.firstName.toLowerCase().contains(currentValue)
                                            || chosenStudent.lastName.toLowerCase().contains(currentValue)
                                            || chosenStudent.nickname.toLowerCase().contains(currentValue))
                                    }

                                    if (autoCompletePopup.filteredSuggestions.isEmpty() || this.text.isEmpty()) {
                                        autoCompletePopup.hide()
                                        // if you remove textField.getText.isEmpty()
                                        // when text field is empty it suggests all options
                                        // so you can choose
                                    } else {
                                        autoCompletePopup.show(this)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}