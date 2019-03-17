package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.controllers.SMControllerCentral
import com.ebolo.studentmanager.models.SMUserModel
import javafx.geometry.Orientation
import tornadofx.*

class SMLoginFormView : View("StuMan v0.0.1-SNAPSHOT") {
    private val controllerCentral: SMControllerCentral by di()

    val user = SMUserModel()

    override val root = form {
        fieldset("Trung tâm giảng dạy", labelPosition = Orientation.VERTICAL) {
            field("Tên người dùng") {
                textfield(user.username).required()
            }
            field("Mật khẩu") {
                passwordfield(user.password).required()
            }
            button("Đăng nhập") {
                action {
                    if (controllerCentral.userController.login(user.getEntity())) {
                        replaceWith<SMMainView>(
                            sizeToScene = true,
                            centerOnScreen = true
                        )

                        currentStage?.isMaximized = true
                    }
                }
            }
        }
    }
}