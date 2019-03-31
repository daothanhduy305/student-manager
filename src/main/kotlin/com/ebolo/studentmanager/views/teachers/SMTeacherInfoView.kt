package com.ebolo.studentmanager.views.teachers

import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTeacherRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import javafx.beans.binding.Bindings
import javafx.geometry.Orientation
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import tornadofx.*

class SMTeacherInfoView : View("Thông tin giáo viên") {
    private val teacherModel: SMTeacherModel by inject()

    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param()

    override val root = form {
        hbox {
            vbox {
                fieldset(labelPosition = Orientation.HORIZONTAL) {
                    field("Tên") {
                        textfield(teacherModel.firstName).required()
                    }

                    field("Họ và tên lót") {
                        textfield(teacherModel.lastName).required()
                    }

                    field("Ngày sinh") {
                        datepicker(teacherModel.birthday)
                    }

                    field("Số điện thoại") {
                        textfield(teacherModel.phone).required()
                    }

                    field("Địa chỉ") {
                        textfield(teacherModel.address)
                    }
                }
            }

            vbox {
                paddingLeft = 15.0
                spacing = 10.0

                button("Hoàn tất") {
                    vgrow = Priority.ALWAYS
                    useMaxWidth = true

                    enableWhen(Bindings.and(teacherModel.dirty, teacherModel.valid))

                    action {
                        // base on the crud mode, we define the appropriate action
                        val result: SMCRUDUtils.SMCRUDResult = when (mode) {
                            SMCRUDUtils.CRUDMode.NEW -> serviceCentral.teacherService.createNewTeacher(teacherModel)
                            SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.teacherService.editTeacher(teacherModel)
                            else -> {
                                error("Đã xảy ra lỗi", "Unsupported CRUD mode", ButtonType.CLOSE)
                                SMCRUDUtils.SMCRUDResult(false)
                            }
                        }
                        // refresh if success
                        if (result.success) {
                            fire(SMTeacherRefreshRequest)
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

            width
        }
    }

    override fun onDock() {
        super.onDock()
        if (mode == SMCRUDUtils.CRUDMode.NEW) {
            teacherModel.item = SMTeacherModel.SMTeacherDto()
        }
    }
}