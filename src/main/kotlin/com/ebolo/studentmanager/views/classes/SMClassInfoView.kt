package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.utils.SMCRUDUtils
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class SMClassInfoView : View("Thông tin lớp học") {
    private val classModel: SMClassModel by inject()

    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param()

    private val subjectList by lazy { FXCollections.observableList(serviceCentral.subjectService.getAllAvailableSubjects()) }
    private val teacherList by lazy { FXCollections.observableList(serviceCentral.teacherService.getTeacherList()) }

    override val root = form {
        hbox {
            vbox {
                fieldset(labelPosition = Orientation.HORIZONTAL) {
                    field("Tên lớp") {
                        textfield(classModel.name).required()
                    }

                    field("Môn học") {
                        combobox(classModel.subject, values = subjectList) {
                            cellFormat { subject -> text = subject.name }
                            vgrow = Priority.ALWAYS
                            useMaxWidth = true
                        }.required()
                    }

                    field("Giáo viên") {
                        combobox(classModel.teacher, values = teacherList) {
                            cellFormat { teacher -> text = "${teacher.lastName} ${teacher.firstName}" }
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
                        /*val result: SMCRUDUtils.SMCRUDResult = when (mode) {
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
                        }*/
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

    override fun onDock() {
        super.onDock()
        if (mode == SMCRUDUtils.CRUDMode.NEW) {
            classModel.item = SMClassModel.SMClassDto()
        }
    }
}