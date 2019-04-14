package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*

class SMLoginFormView : View("StuMan v0.0.1-SNAPSHOT") {
    private val serviceCentral: SMServiceCentral by di()

    private val user = SMUserModel()

    override val root = form {
        prefWidth = 300.0

        fieldset("Trung tâm giảng dạy", labelPosition = Orientation.VERTICAL) {
            field("Tên người dùng") {
                this += JFXTextField().apply {
                    bind(user.username)
                    required()
                }
            }

            field("Mật khẩu") {
                this += JFXTextField().apply {
                    bind(user.password)
                    required()
                }
            }

            hbox(spacing = 10) {
                paddingTop = 20
                alignment = Pos.CENTER_RIGHT

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