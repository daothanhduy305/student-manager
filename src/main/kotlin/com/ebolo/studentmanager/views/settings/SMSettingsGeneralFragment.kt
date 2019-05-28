package com.ebolo.studentmanager.views.settings

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class SMSettingsGeneralFragment : Fragment() {
    override val root = form {
        vbox {
            vgrow = Priority.ALWAYS

            hbox {
                vgrow = Priority.ALWAYS

                vbox {
                    fieldset(labelPosition = Orientation.VERTICAL, text = "Dữ liệu") {
                        field("Cơ sở dữ liệu") {
                            this += JFXTextField().apply {

                            }
                        }
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
                        backgroundColor += c("#ff5533")
                        textFill = c("#fff")
                    }

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

                    action {

                    }
                }
            }
        }
    }
}
