package com.ebolo.studentmanager.views.settings

import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTheme
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*

class SMAddUserFragment : Fragment() {
    private val userModel: SMUserModel by param(SMUserModel())
    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param(SMCRUDUtils.CRUDMode.NEW)

    private val isNotInProgress = SimpleBooleanProperty(true)
    private var hasError = false
    private var errorMessage = ""

    override val root = form {
        style {
            backgroundColor += c("#fff")
        }

        fieldset(when (mode) {
            SMCRUDUtils.CRUDMode.NEW -> "Thêm người dùng mới"
            else -> "Thông tin người dùng"
        }, labelPosition = Orientation.HORIZONTAL) {
            vbox(spacing = 20, alignment = Pos.CENTER_RIGHT) {
                field("Tên đăng nhập *") {
                    this += JFXTextField().apply {
                        bind(userModel.username)

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
                }

                field("Mật khẩu *") {
                    this += JFXPasswordField().apply {
                        bind(userModel.password)

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
                }

                hbox(spacing = 20) {
                    alignment = Pos.CENTER_RIGHT

                    this += JFXButton("Hủy bỏ").apply {
                        useMaxWidth = true
                        buttonType = JFXButton.ButtonType.RAISED
                        paddingVertical = 15
                        paddingHorizontal = 30

                        style {
                            backgroundColor += c(SMTheme.CANCEL_BUTTON_COLOR)
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

                        enableWhen(Bindings.and(Bindings.and(userModel.valid, userModel.dirty), isNotInProgress))

                        action {
                            isNotInProgress.value = false

                            runAsync {
                                serviceCentral.userService.createNewOrUpdateUser(userModel)
                            } ui {
                                if (it.success) {
                                    close()
                                } else {
                                    isNotInProgress.value = true
                                    hasError = true
                                    errorMessage = it.errorMessage
                                    userModel.validate()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}