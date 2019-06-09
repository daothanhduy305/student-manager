package com.ebolo.studentmanager.views.utils.ui

import com.ebolo.studentmanager.services.SMGlobal
import javafx.geometry.Pos
import tornadofx.*

class SMErrorsDialog : Fragment(SMGlobal.APP_NAME) {
    private val dialogTitle: String by param()
    private val errorList: List<String> by param()

    override val root = borderpane {
        paddingAll = 20
        prefWidth = 550.0

        usePrefWidth = true

        center {
            hbox(spacing = 36) {
                paddingAll = 20

                vbox(spacing = 20) {
                    alignment = Pos.CENTER

                    stackpane {
                        paddingRight = 16

                        imageview("images/icon.png", true)
                    }
                }

                vbox(spacing = 20) {
                    alignment = Pos.CENTER_LEFT

                    label(dialogTitle) {
                        style {
                            fontSize = Dimension(24.0, Dimension.LinearUnits.pt)
                        }
                    }

                    vbox(spacing = 5) {
                        paddingBottom = 20

                        val errorFontSize = Dimension(12.0, Dimension.LinearUnits.pt)

                        errorList.forEach {
                            label("- $it") {
                                style {
                                    textFill = c("ff0000")
                                    fontSize = errorFontSize
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
