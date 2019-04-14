package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.jfoenix.controls.JFXButton
import javafx.application.Platform
import javafx.geometry.Orientation
import tornadofx.*

class SMLoginFormView : View("StuMan v0.0.1-SNAPSHOT") {
    private val serviceCentral: SMServiceCentral by di()

    private val user = SMUserModel()

    override val root = form {
        fieldset("Trung tâm giảng dạy", labelPosition = Orientation.VERTICAL) {
            field("Tên người dùng") {
                textfield(user.username).required()
            }

            field("Mật khẩu") {
                passwordfield(user.password).required()
            }

            hbox(spacing = 10) {
                paddingTop = 10

                this += JFXButton("Đăng nhập").apply {
                    buttonType = JFXButton.ButtonType.RAISED

                    style {
                        backgroundColor += c("#ffffff")
                    }

                    enableWhen(user.valid)
                    action {
                        if (serviceCentral.userService.login(user.getEntity())) {
                            replaceWith<SMMainView>(
                                sizeToScene = true,
                                centerOnScreen = true
                            )

                            currentStage?.isMaximized = true
                        }
                    }
                }

                this += JFXButton("Thoát").apply {
                    buttonType = JFXButton.ButtonType.RAISED

                    style {
                        backgroundColor += c("#ffffff")
                    }

                    action {
                        Platform.exit()
                        System.exit(0)
                    }
                }
            }
        }
    }
}