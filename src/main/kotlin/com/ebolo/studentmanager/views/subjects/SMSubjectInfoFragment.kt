package com.ebolo.studentmanager.views.subjects

import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*

class SMSubjectInfoFragment : Fragment() {
    private val subjectModel: SMSubjectModel by param(SMSubjectModel())
    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param(SMCRUDUtils.CRUDMode.NEW)

    private val isNotInProgress = SimpleBooleanProperty(true)
    private var hasError = false
    private var errorMessage = ""

    private var subjectNameBox by singleAssign<JFXTextField>()

    override val root = form {
        style {
            backgroundColor += c("#fff")
        }

        fieldset(when (mode) {
            SMCRUDUtils.CRUDMode.NEW -> "Thêm môn học mới"
            else -> "Thông tin môn"
        }, labelPosition = Orientation.HORIZONTAL) {
            vbox(spacing = 20, alignment = Pos.CENTER_RIGHT) {
                field("Tên môn *") {
                    subjectNameBox = JFXTextField().apply {
                        bind(subjectModel.name)

                        enableWhen(isNotInProgress)

                        required()

                        // Add custom validation for any error message that we might have encountered
                        validator {
                            if (hasError) {
                                hasError = false // reset the state
                                error(errorMessage)
                            } else null
                        }
                    }

                    this += subjectNameBox
                }

                hbox(spacing = 20) {
                    alignment = Pos.CENTER_RIGHT

                    this += JFXButton("Hủy bỏ").apply {
                        useMaxWidth = true
                        buttonType = JFXButton.ButtonType.RAISED
                        paddingVertical = 15
                        paddingHorizontal = 30

                        style {
                            backgroundColor += c("#ff5533")
                            textFill = c("#fff")
                        }

                        enableWhen(isNotInProgress)

                        action {
                            close()
                        }
                    }

                    this += JFXButton("Hoàn tất").apply {
                        useMaxWidth = true
                        buttonType = JFXButton.ButtonType.RAISED
                        paddingVertical = 15
                        paddingHorizontal = 30

                        style {
                            backgroundColor += c("#fff")
                        }

                        enableWhen(Bindings.and(Bindings.and(subjectModel.valid, subjectModel.dirty), isNotInProgress))

                        action {
                            isNotInProgress.value = false

                            runAsync {
                                serviceCentral.subjectService.createNewOrUpdateSubject(subjectModel)
                            } ui {
                                if (it.success) {
                                    close()
                                } else {
                                    isNotInProgress.value = true
                                    hasError = true
                                    errorMessage = it.errorMessage
                                    subjectModel.validate()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}