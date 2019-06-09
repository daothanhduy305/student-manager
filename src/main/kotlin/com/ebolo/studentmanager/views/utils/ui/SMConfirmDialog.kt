package com.ebolo.studentmanager.views.utils.ui

import com.ebolo.studentmanager.services.SMGlobal
import com.jfoenix.controls.JFXButton
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class SMConfirmDialog : Fragment(SMGlobal.APP_NAME) {
    private val dialogContent: String by param()
    private val onOKClicked: (() -> Unit)? by param()

    override val root = borderpane {
        prefWidth = 400.0

        center {
            vbox {
                paddingTop = 40.0
                paddingBottom = 20.0
                paddingHorizontal = 40

                label(dialogContent) {
                    style {
                        fontSize = Dimension(14.0, Dimension.LinearUnits.pt)
                    }
                }
            }
        }

        bottom {
            hbox(spacing = 20) {
                paddingAll = 20
                hgrow = Priority.ALWAYS
                alignment = Pos.CENTER_RIGHT

                this += JFXButton("Hủy bỏ").apply {
                    useMaxWidth = true
                    buttonType = JFXButton.ButtonType.RAISED
                    paddingVertical = 15
                    paddingHorizontal = 30

                    action { close() }

                    style {
                        backgroundColor += c("#ff5533")
                        textFill = c("#fff")
                    }
                }

                this += JFXButton("OK").apply {
                    useMaxWidth = true
                    buttonType = JFXButton.ButtonType.RAISED
                    paddingVertical = 15
                    paddingHorizontal = 30

                    action {
                        close()
                        onOKClicked?.invoke()
                    }

                    style {
                        backgroundColor += c("#ffffff")
                    }
                }
            }
        }
    }
}
