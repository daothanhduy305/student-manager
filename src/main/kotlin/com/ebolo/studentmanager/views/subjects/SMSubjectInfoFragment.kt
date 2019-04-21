package com.ebolo.studentmanager.views.subjects

import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*

class SMSubjectInfoFragment : Fragment() {
    private val subjectModel: SMSubjectModel by param(SMSubjectModel())
    private val serviceCentral: SMServiceCentral by di()

    override val root = form {
        style {
            backgroundColor += c("#fff")
        }

        fieldset("Thêm môn học mới", labelPosition = Orientation.HORIZONTAL) {
            field("Tên môn") {
                this += JFXTextField().apply {
                    bind(subjectModel.name)
                    required()
                }
            }

            vbox(spacing = 10, alignment = Pos.CENTER_RIGHT) {
                hbox(spacing = 10) {
                    paddingTop = 10
                    alignment = Pos.CENTER_RIGHT

                    this += JFXButton("Hủy bỏ").apply {
                        buttonType = JFXButton.ButtonType.RAISED

                        style {
                            backgroundColor += c("#fff")
                        }

                        action {
                            close()
                        }
                    }

                    this += JFXButton("Hoàn tất").apply {
                        buttonType = JFXButton.ButtonType.RAISED

                        style {
                            backgroundColor += c("#fff")
                        }

                        enableWhen(subjectModel.valid)

                        action {
                            var result: SMCRUDUtils.SMCRUDResult = SMCRUDUtils.SMCRUDResult(false)
                            runAsync {
                                result = serviceCentral.subjectService.createNewSubject(subjectModel)
                            }.ui {
                                if (result.success) {
                                    close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}