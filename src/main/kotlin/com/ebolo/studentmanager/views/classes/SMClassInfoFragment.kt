package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.services.SMClassListRefreshRequest
import com.ebolo.studentmanager.services.SMClassRefreshEvent
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.*
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

    private var teacherComboBox by singleAssign<JFXComboBox<SMTeacherModel.SMTeacherDto>>()
    private var subjectComboBox by singleAssign<JFXComboBox<SMSubjectModel.SMSubjectDto>>()

    override val root = stackpane {
        this += JFXTabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab("Thông tin lớp") {
                form {
                    hbox {
                        vbox {
                            fieldset(labelPosition = Orientation.HORIZONTAL) {
                                field("Tên lớp") {
                                    this += JFXTextField().apply {
                                        bind(classModel.name)
                                        required()
                                    }
                                }

                                field("Môn học") {
                                    subjectComboBox = JFXComboBox(subjectList).apply {
                                        bind(classModel.subject)

                                        if (mode != SMCRUDUtils.CRUDMode.NEW) {
                                            value = subjectList.first { it.id == classModel.item.subject.id }
                                        }

                                        cellFormat { subject -> text = subject.name }
                                        vgrow = Priority.ALWAYS
                                        useMaxWidth = true

                                        required()
                                    }

                                    this += subjectComboBox
                                }

                                field("Giáo viên") {
                                    teacherComboBox = JFXComboBox(teacherList).apply {
                                        bind(classModel.teacher)

                                        if (mode != SMCRUDUtils.CRUDMode.NEW) {
                                            value = teacherList.first { it.id == classModel.item.teacher.id }
                                        }
                                        cellFormat { teacher ->
                                            text = "${teacher.lastName} ${teacher.firstName}"
                                        }
                                        vgrow = Priority.ALWAYS
                                        useMaxWidth = true

                                        required()
                                    }

                                    this += teacherComboBox
                                }

                                field("Ngày bắt đầu") {
                                    this += JFXDatePicker().apply {
                                        bind(classModel.startDate)

                                        defaultColor = c("#3f51b5")
                                        isOverLay = true

                                        required()
                                    }
                                    //datepicker(classModel.startDate).required()
                                }

                                field("Học phí") {
                                    this += JFXTextField().apply {
                                        bind(classModel.tuitionFee)

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
                                    this += JFXTextField().apply {
                                        bind(classModel.numberOfExams)
                                        required()
                                    }
                                }
                            }
                        }

                        vbox {
                            paddingLeft = 15.0
                            spacing = 10.0

                            this += JFXButton("Hoàn tất").apply {
                                vgrow = Priority.ALWAYS
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED

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
                                        fire(SMClassListRefreshRequest)
                                        modalStage?.close()
                                    } else {
                                        error("Đã xảy ra lỗi", result.errorMessage, ButtonType.CLOSE)
                                    }
                                }

                                style {
                                    backgroundColor += c("#ffffff")
                                }
                            }

                            this += JFXButton("Hủy bỏ").apply {
                                vgrow = Priority.ALWAYS
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED

                                action { modalStage?.close() }

                                style {
                                    backgroundColor += c("#ffffff")
                                }
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
                                            //classModel.item.studentList.add(chosenStudent)

                                            // Register this student into class
                                            runAsync {
                                                with(serviceCentral.classService) {
                                                    chosenStudent registerToClass classModel
                                                }
                                            }
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

            // Re-bind the value for the teacher and subject combo boxes
            subscribe<SMClassRefreshEvent> { event ->
                teacherComboBox.value = teacherList.first {
                    event.classDto.teacher.id == it.id
                }

                subjectComboBox.value = subjectList.first {
                    event.classDto.subject.id == it.id
                }
            }
        }
    }
}