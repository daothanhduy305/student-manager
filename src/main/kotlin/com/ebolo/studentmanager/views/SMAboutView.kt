package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.services.SMGlobal.APP_NAME
import com.ebolo.studentmanager.services.SMServiceCentral
import javafx.geometry.Pos
import tornadofx.*

class SMAboutView : View("Giới thiệu") {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        setPrefSize(500.0, 300.0)

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

                    label(APP_NAME) {
                        style {
                            fontSize = Dimension(24.0, Dimension.LinearUnits.pt)
                        }
                    }

                    vbox(spacing = 10) {
                        label("Phiên bản: ${serviceCentral.version}") {
                            style {
                                fontSize = Dimension(12.0, Dimension.LinearUnits.pt)
                            }
                        }

                        label("Tác giả: `ebolo` team") {
                            style {
                                fontSize = Dimension(12.0, Dimension.LinearUnits.pt)
                            }
                        }
                    }
                }
            }

        }
    }
}
