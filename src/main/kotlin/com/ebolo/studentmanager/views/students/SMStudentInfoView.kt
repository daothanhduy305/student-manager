package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.entities.EducationLevel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMServiceCentral
import javafx.geometry.Orientation
import javafx.scene.control.TabPane
import tornadofx.*

class SMStudentInfoView : View("Thông tin học viên") {
    private val studentModel: SMStudentModel = SMStudentModel()
    private val serviceCentral: SMServiceCentral by di()

    override val root = tabpane {
        // Don't support closing tabs
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        tab("Thông tin cá nhân") {
            form {
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
        }

        tab("Thông tin học phí") {

        }
    }
}