package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMStudentModel
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextArea
import com.jfoenix.controls.JFXTextField
import javafx.geometry.Orientation
import tornadofx.*

class SMClassPerformanceFragment : Fragment() {
    private val studentInfo: SMStudentModel.SMStudentDto by param()
    private val performanceInfo: SMStudentPerformanceInfo by param()
    private val classId: String by param()

    override val root = hbox {
        form {
            fieldset(labelPosition = Orientation.HORIZONTAL) {
                for (i in 0..(studentInfo.gradeList.size - 1)) {
                    field("Cột điểm ${i + 1}") {
                        this += JFXTextField().apply {
                            if (studentInfo.gradeList[i] > -1) {
                                text = studentInfo.gradeList[i].toString()
                            }
                        }
                    }
                }

                field("Nhận xét") {
                    this += JFXTextArea().apply {
                        text = performanceInfo.note
                    }
                }
            }
        }

        vbox(spacing = 20) {
            paddingAll = 20

            this += JFXButton("Lưu lại").apply {
                buttonType = JFXButton.ButtonType.RAISED

                style {
                    backgroundColor += c("#ffffff")
                }
            }

            this += JFXButton("Hủy bỏ").apply {
                buttonType = JFXButton.ButtonType.RAISED

                style {
                    backgroundColor += c("#ffffff")
                }
            }
        }
    }
}