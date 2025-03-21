package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.entities.EducationLevel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMStudentRefreshRequest
import com.ebolo.studentmanager.services.SMTheme
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.DateCell
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.util.Callback
import tornadofx.*
import java.time.LocalDate

class SMStudentInfoFragment : Fragment("Thông tin học viên") {
    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param()
    private val studentModel: SMStudentModel by param(SMStudentModel())

    private val isProcessing = SimpleBooleanProperty(false)

    override val root = stackpane {
        title = when (mode) {
            SMCRUDUtils.CRUDMode.NEW -> "Thêm học viên"
            else -> "Thông tin học viên ${studentModel.lastName.value} ${studentModel.firstName.value}"
        }

        style {
            backgroundColor += c("#fff")
        }

        this += JFXTabPane().apply {
            // Don't support closing tabs
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab("Thông tin cá nhân") {
                form {
                    paddingAll = 20

                    vbox {
                        vgrow = Priority.ALWAYS

                        vbox {
                            vgrow = Priority.ALWAYS

                            fieldset(labelPosition = Orientation.HORIZONTAL) {
                                spacing += 20.0

                                field("Tên *") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.firstName)

                                        required()
                                    }
                                }

                                field("Họ và tên lót *") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.lastName)

                                        required()
                                    }
                                }

                                field("Nickname") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.nickname)
                                    }
                                }

                                field("Học vấn *") {
                                    this += JFXComboBox(EducationLevel.values().toList().asObservable()).apply {
                                        bind(studentModel.educationLevel)
                                        cellFormat { text = it.title }

                                        required()
                                    }
                                }

                                field("Cấp độ") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.studyLevel)
                                    }
                                }

                                field("Ngày sinh") {
                                    this += JFXDatePicker().apply {
                                        bind(studentModel.birthday)

                                        dayCellFactory = Callback {
                                            object : DateCell() {
                                                override fun updateItem(item: LocalDate?, empty: Boolean) {
                                                    super.updateItem(item, empty)
                                                    isDisable = empty || item!!.isAfter(LocalDate.now())
                                                }
                                            }
                                        }

                                        defaultColor = c("#3f51b5")
                                        isOverLay = false
                                    }
                                }

                                field("Số điện thoại") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.phone)
                                    }
                                }

                                field("Số điện thoại phụ huynh") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.parentPhone)
                                    }
                                }

                                field("Địa chỉ") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.address)
                                    }
                                }
                            }
                        }

                        hbox {
                            alignment = Pos.BOTTOM_RIGHT
                            spacing = 20.0
                            paddingVertical = 20

                            this += JFXButton("Hủy bỏ").apply {
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED
                                paddingVertical = 15
                                paddingHorizontal = 30

                                style {
                                    backgroundColor += c(SMTheme.CANCEL_BUTTON_COLOR)
                                    textFill = c("#fff")
                                }

                                enableWhen(isProcessing.not())

                                action { modalStage?.close() }
                            }

                            this += JFXButton("Hoàn tất").apply {
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED
                                paddingVertical = 15
                                paddingHorizontal = 30

                                style {
                                    backgroundColor += c("#fff")
                                }

                                enableWhen(studentModel.dirty.and(studentModel.valid).and(isProcessing.not()))

                                action {
                                    isProcessing.value = true
                                    StudentManagerApplication.startSync()
                                    // base on the crud mode, we define the appropriate action
                                    runAsync {
                                        when (mode) {
                                            SMCRUDUtils.CRUDMode.NEW -> serviceCentral.studentService.createNewStudent(studentModel)
                                            SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.studentService.editStudent(studentModel)
                                            else -> {
                                                SMCRUDUtils.SMCRUDResult(false, "Unsupported CRUD mode")
                                            }
                                        }
                                    } ui {
                                        isProcessing.value = false
                                        StudentManagerApplication.stopSync()
                                        // refresh if success
                                        if (it.success) {
                                            fire(SMStudentRefreshRequest())
                                            modalStage?.close()
                                        } else {
                                            error("Đã xảy ra lỗi", it.errorMessage, ButtonType.CLOSE)
                                        }
                                    }
                                }
                            }

                            if (mode == SMCRUDUtils.CRUDMode.NEW || mode == SMCRUDUtils.CRUDMode.EDIT) {
                                val buttonTitle = when (mode) {
                                    SMCRUDUtils.CRUDMode.NEW -> "Tiếp tục thêm"
                                    SMCRUDUtils.CRUDMode.EDIT -> "Tạo lặp"
                                    else -> ""
                                }
                                this += JFXButton(buttonTitle).apply {
                                    useMaxWidth = true
                                    buttonType = JFXButton.ButtonType.RAISED
                                    paddingVertical = 15
                                    paddingHorizontal = 30

                                    style {
                                        backgroundColor += c("#fff")
                                    }

                                    enableWhen(studentModel.dirty.or(SimpleBooleanProperty(mode == SMCRUDUtils.CRUDMode.EDIT)).and(studentModel.valid).and(isProcessing.not()))

                                    action {
                                        isProcessing.value = true
                                        StudentManagerApplication.startSync()
                                        // base on the crud mode, we define the appropriate action
                                        runAsync {
                                            when (mode) {
                                                SMCRUDUtils.CRUDMode.NEW -> serviceCentral.studentService.createNewStudent(studentModel)
                                                SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.studentService.editStudent(studentModel)
                                                else -> {
                                                    SMCRUDUtils.SMCRUDResult(false, "Unsupported CRUD mode")
                                                }
                                            }
                                        } ui {
                                            isProcessing.value = false
                                            StudentManagerApplication.stopSync()
                                            // refresh if success
                                            if (it.success) {
                                                fire(SMStudentRefreshRequest())
                                                modalStage?.close()
                                                find<SMStudentInfoFragment>(
                                                    "mode" to SMCRUDUtils.CRUDMode.NEW,
                                                    "studentModel" to studentModel.apply {
                                                        id.value = null
                                                    }
                                                ).openModal()
                                            } else {
                                                error("Đã xảy ra lỗi", it.errorMessage, ButtonType.CLOSE)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (mode != SMCRUDUtils.CRUDMode.NEW) {
                tab("Lớp đã đăng ký") {
                    borderpane {
                        center = find<SMRegisteredClassesFragment>(
                            "studentModel" to studentModel
                        ).root
                    }
                }
            }
        }
    }
}