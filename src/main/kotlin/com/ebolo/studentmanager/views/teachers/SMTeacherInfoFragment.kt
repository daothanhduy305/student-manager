package com.ebolo.studentmanager.views.teachers

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTeacherRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDatePicker
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.DateCell
import javafx.scene.layout.Priority
import tornadofx.*
import java.time.LocalDate

class SMTeacherInfoFragment : Fragment("Thông tin giáo viên") {
    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param(SMCRUDUtils.CRUDMode.NEW)
    private val teacherModel: SMTeacherModel by param(SMTeacherModel())

    private val isProcessing = SimpleBooleanProperty(false)

    override val root = form {
        paddingAll = 20

        style {
            backgroundColor += c("#fff")
        }

        vbox {
            vgrow = Priority.ALWAYS

            vbox {
                vgrow = Priority.ALWAYS

                fieldset("Thông tin chung", labelPosition = Orientation.HORIZONTAL) {
                    spacing = 20.0

                    field("Tên *") {
                        this += JFXTextField().apply {
                            bind(teacherModel.firstName)
                            required()
                        }
                    }

                    field("Họ và tên lót *") {
                        this += JFXTextField().apply {
                            bind(teacherModel.lastName)
                            required()
                        }
                    }

                    field("Ngày sinh") {
                        this += JFXDatePicker().apply {
                            bind(teacherModel.birthday)

                            setDayCellFactory {
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
                            bind(teacherModel.phone)
                        }
                    }

                    field("Địa chỉ") {
                        this += JFXTextField().apply {
                            bind(teacherModel.address)
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
                        backgroundColor += c("#ff5533")
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

                    enableWhen(teacherModel.dirty.and(teacherModel.valid).and(isProcessing.not()))

                    action {
                        isProcessing.value = true
                        StudentManagerApplication.startSync()
                        // base on the crud mode, we define the appropriate action
                        runAsync {
                            when (mode) {
                                SMCRUDUtils.CRUDMode.NEW -> serviceCentral.teacherService.createNewTeacher(teacherModel)
                                SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.teacherService.editTeacher(teacherModel)
                                else -> {
                                    SMCRUDUtils.SMCRUDResult(false, "Unsupported CRUD mode")
                                }
                            }
                        } ui {
                            isProcessing.value = false
                            StudentManagerApplication.stopSync()
                            // refresh if success
                            if (it.success) {
                                fire(SMTeacherRefreshRequest())
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

                        enableWhen(teacherModel.dirty.or(SimpleBooleanProperty(mode == SMCRUDUtils.CRUDMode.EDIT)).and(teacherModel.valid).and(isProcessing.not()))

                        action {
                            isProcessing.value = true
                            StudentManagerApplication.startSync()
                            // base on the crud mode, we define the appropriate action
                            runAsync {
                                when (mode) {
                                    SMCRUDUtils.CRUDMode.NEW -> serviceCentral.teacherService.createNewTeacher(teacherModel)
                                    SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.teacherService.editTeacher(teacherModel)
                                    else -> {
                                        SMCRUDUtils.SMCRUDResult(false, "Unsupported CRUD mode")
                                    }
                                }
                            } ui {
                                isProcessing.value = false
                                StudentManagerApplication.stopSync()
                                // refresh if success
                                if (it.success) {
                                    fire(SMTeacherRefreshRequest())
                                    modalStage?.close()
                                    find<SMTeacherInfoFragment>(
                                        "mode" to SMCRUDUtils.CRUDMode.NEW,
                                        "studentModel" to teacherModel.apply {
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