package com.ebolo.studentmanager.views.students

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
                        textfield(studentModel.nickname).required()
                    }

                    field("Ngày sinh") {
                        datepicker {
                            valueProperty().bindBidirectional(studentModel.birthday)
                        }
                    }
                }
            }
        }

        tab("Thông tin học phí") {

        }
    }
}