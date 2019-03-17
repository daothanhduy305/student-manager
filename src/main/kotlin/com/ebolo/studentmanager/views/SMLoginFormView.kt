package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.services.SMServiceCentral
import javafx.geometry.Orientation
import tornadofx.*

class SMLoginFormView : View("StuMan v0.0.1-SNAPSHOT") {
    private val serviceCentral: SMServiceCentral by di()

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
                    if (serviceCentral.userService.login(user.getEntity())) {
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