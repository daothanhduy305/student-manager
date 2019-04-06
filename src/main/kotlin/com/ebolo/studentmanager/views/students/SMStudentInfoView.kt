package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.entities.EducationLevel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMStudentRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import javafx.beans.binding.Bindings
import javafx.geometry.Orientation
import javafx.scene.control.ButtonType
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*

class SMStudentInfoView : View("Thông tin học viên") {
    private val studentModel: SMStudentModel by inject()

    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param()

    override val root = tabpane {
        // Don't support closing tabs
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        tab("Thông tin cá nhân") {
            form {
                hbox {
                    vbox {
                        fieldset(labelPosition = Orientation.HORIZONTAL) {
                            field("Tên") {
                                textfield(studentModel.firstName).required()
                            }

                            field("Họ và tên lót") {
                                textfield(studentModel.lastName).required()
                            }

                            field("Nickname") {
                                textfield(studentModel.nickname)
                            }

                            field("Ngày sinh") {
                                datepicker(studentModel.birthday)
                            }

                            field("Số điện thoại") {
                                textfield(studentModel.phone).required()
                            }

                            field("Số điện thoại phụ huynh") {
                                textfield(studentModel.parentPhone)
                            }

                            field("Địa chỉ") {
                                textfield(studentModel.address)
                            }

                            field("Học vấn") {
                                combobox(
                                    studentModel.educationLevel,
                                    values = EducationLevel.values().toList()
                                ) {
                                    cellFormat { text = it.title }
                                }
                            }
                        }
                    }

                    vbox {
                        paddingLeft = 15.0
                        spacing = 10.0

                        button("Hoàn tất") {
                            vgrow = Priority.ALWAYS
                            useMaxWidth = true

                            enableWhen(Bindings.and(studentModel.dirty, studentModel.valid))

                            action {
                                // base on the crud mode, we define the appropriate action
                                val result: SMCRUDUtils.SMCRUDResult = when (mode) {
                                    SMCRUDUtils.CRUDMode.NEW -> serviceCentral.studentService.createNewStudent(studentModel)
                                    SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.studentService.editStudent(studentModel)
                                    else -> {
                                        error("Đã xảy ra lỗi", "Unsupported CRUD mode", ButtonType.CLOSE)
                                        SMCRUDUtils.SMCRUDResult(false)
                                    }
                                }
                                // refresh if success
                                if (result.success) {
                                    fire(SMStudentRefreshRequest)
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

        tab("Thông tin học phí") {

        }
    }

    override fun onDock() {
        super.onDock()
        if (mode == SMCRUDUtils.CRUDMode.NEW) {
            studentModel.item = SMStudentModel.SMStudentDto()
        }
    }
}