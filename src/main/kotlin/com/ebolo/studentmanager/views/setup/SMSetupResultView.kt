package com.ebolo.studentmanager.views.setup

import com.ebolo.studentmanager.StudentManagerApplication
import com.jfoenix.controls.JFXButton
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

/**
 * View to display the result of setup process of the application
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMSetupResultView : View("Student Manager") {
    override val root = borderpane {
        paddingAll = 20
        prefWidth = 500.0

        val setupResult = (app as StudentManagerApplication).setupResult

        top {
            vbox {
                label("Không thể khởi tạo chương trình") {
                    style {
                        fontSize = Dimension(20.0, Dimension.LinearUnits.pt)
                    }
                }
            }
        }

        center {
            vbox(spacing = 20) {
                paddingTop = 40
                val failedDatabaseCheck = setupResult.errors.contains(StudentManagerApplication.SetupError.DATABASE_ERROR)

                label("- Cơ sở dữ liệu: ${
                if (failedDatabaseCheck) {
                    "thất bại"
                } else {
                    "thành công"
                }
                }") {
                    style {
                        fontSize = Dimension(16.0, Dimension.LinearUnits.pt)
                        if (failedDatabaseCheck) {
                            textFill = c("ff0000")
                        }
                    }
                }

                val failedMasterAccountCheck = setupResult.errors.contains(StudentManagerApplication.SetupError.MASTER_ACCOUNT)

                label("- Tài khoản Master: ${
                if (failedMasterAccountCheck) {
                    "thất bại"
                } else {
                    "thành công"
                }
                }") {
                    style {
                        fontSize = Dimension(16.0, Dimension.LinearUnits.pt)
                        if (failedMasterAccountCheck) {
                            textFill = c("ff0000")
                        }
                    }
                }

                hbox {
                    alignment = Pos.CENTER_RIGHT
                    paddingTop = 20
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Cài đặt").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true
                        paddingVertical = 15
                        paddingHorizontal = 30

                        style {
                            backgroundColor += c("#fff")
                        }

                        action {
                            replaceWith<SMSetupFragment>(centerOnScreen = true, sizeToScene = true)
                        }
                    }
                }
            }
        }
    }
}
