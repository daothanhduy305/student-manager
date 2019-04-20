package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.entities.EducationLevel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMStudentRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.*
import javafx.beans.binding.Bindings
import javafx.geometry.Orientation
import javafx.scene.control.ButtonType
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*

class SMStudentInfoFragment : Fragment("Thông tin học viên") {
    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param()
    private val studentModel: SMStudentModel by param(SMStudentModel())

    override val root = stackpane {

        style {
            backgroundColor += c("#fff")
        }

        this += JFXTabPane().apply {
            // Don't support closing tabs
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab("Thông tin cá nhân") {
                form {
                    paddingAll = 20

                    hbox {
                        vbox {
                            fieldset(labelPosition = Orientation.HORIZONTAL) {
                                spacing += 20.0

                                field("Tên") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.firstName)

                                        required()
                                    }
                                }

                                field("Họ và tên lót") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.lastName)

                                        required()
                                    }
                                }

                                field("Nickname") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.nickname)

                                        required()
                                    }
                                }

                                field("Ngày sinh") {
                                    this += JFXDatePicker().apply {
                                        bind(studentModel.birthday)

                                        defaultColor = c("#3f51b5")
                                        isOverLay = false
                                    }
                                }

                                field("Số điện thoại") {
                                    this += JFXTextField().apply {
                                        bind(studentModel.phone)

                                        required()
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

                                field("Học vấn") {
                                    this += JFXComboBox(EducationLevel.values().toList().observable()).apply {
                                        bind(studentModel.educationLevel)
                                        cellFormat { text = it.title }
                                    }
                                }
                            }
                        }

                        vbox {
                            paddingLeft = 20.0
                            spacing = 10.0

                            this += JFXButton("Hoàn tất").apply {
                                vgrow = Priority.ALWAYS
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED

                                style {
                                    backgroundColor += c("#fff")
                                }

                                enableWhen(Bindings.and(studentModel.dirty, studentModel.valid))

                                action {
                                    // base on the crud mode, we define the appropriate action
                                    val result: SMCRUDUtils.SMCRUDResult = runAsync {
                                        when (mode) {
                                            SMCRUDUtils.CRUDMode.NEW -> serviceCentral.studentService.createNewStudent(studentModel)
                                            SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.studentService.editStudent(studentModel)
                                            else -> {
                                                error("Đã xảy ra lỗi", "Unsupported CRUD mode", ButtonType.CLOSE)
                                                SMCRUDUtils.SMCRUDResult(false)
                                            }
                                        }
                                    }.get()

                                    // refresh if success
                                    if (result.success) {
                                        fire(SMStudentRefreshRequest)
                                        modalStage?.close()
                                    } else {
                                        error("Đã xảy ra lỗi", result.errorMessage, ButtonType.CLOSE)
                                    }
                                }
                            }

                            this += JFXButton("Hủy bỏ").apply {
                                vgrow = Priority.ALWAYS
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED

                                style {
                                    backgroundColor += c("#fff")
                                }

                                action { modalStage?.close() }
                            }
                        }
                    }
                }
            }

            if (mode != SMCRUDUtils.CRUDMode.NEW) {
                tab("Thông tin học phí") {

                }
            }
        }
    }
}