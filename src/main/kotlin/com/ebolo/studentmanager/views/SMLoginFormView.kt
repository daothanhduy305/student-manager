package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.Settings
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class SMLoginFormView : View("Student Manager") {
    private val serviceCentral: SMServiceCentral by di()

    private val user = SMUserModel()
    private val rememberMe = SimpleBooleanProperty()

    override val root = form {
        prefWidth = 300.0

        style {
            backgroundColor += c("#fff")
        }

        fieldset("Trung tâm giảng dạy", labelPosition = Orientation.VERTICAL) {
            vbox(spacing = 20) {
                field("Tên người dùng") {
                    this += JFXTextField().apply {
                        hgrow = Priority.ALWAYS
                        bind(user.username)
                        required()
                    }
                }

                field("Mật khẩu") {
                    this += JFXPasswordField().apply {
                        hgrow = Priority.ALWAYS
                        bind(user.password)
                        required()
                    }
                }

                this += JFXCheckBox("Ghi nhớ").apply {
                    bind(rememberMe)
                }

                hbox(spacing = 20) {
                    alignment = Pos.CENTER_RIGHT

                    this += JFXButton("Đăng nhập").apply {
                        useMaxWidth = true
                        buttonType = JFXButton.ButtonType.RAISED
                        paddingVertical = 15
                        paddingHorizontal = 30

                        style {
                            backgroundColor += c("#ffffff")
                        }

                        enableWhen(user.valid)
                        action {
                            if (serviceCentral.userService.login(user.getEntity())) {
                                if (rememberMe.value) {
                                    StudentManagerApplication.setSettings(
                                        Settings.CREDENTIAL_USERNAME to user.username.value,
                                        Settings.CREDENTIAL_PASSWORD to user.password.value,
                                        Settings.REMEMBER_CREDENTIAL to rememberMe.value
                                    )
                                } else {
                                    StudentManagerApplication.removeSettings(
                                        Settings.CREDENTIAL_USERNAME,
                                        Settings.CREDENTIAL_PASSWORD
                                    )
                                }

                                replaceWith<SMMainView>(
                                    sizeToScene = true,
                                    centerOnScreen = true
                                )

                                primaryStage.isMaximized = true
                            }
                        }
                    }

                    this += JFXButton("Thoát").apply {
                        useMaxWidth = true
                        buttonType = JFXButton.ButtonType.RAISED
                        paddingVertical = 15
                        paddingHorizontal = 30

                        action { modalStage?.close() }

                        style {
                            backgroundColor += c("#ff5533")
                            textFill = c("#fff")
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
}