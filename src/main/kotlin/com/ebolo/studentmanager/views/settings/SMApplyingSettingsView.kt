package com.ebolo.studentmanager.views.settings

import javafx.geometry.Pos
import tornadofx.*

/**
 * A dialog to be shown to the user when the settings are being applied
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMApplyingSettingsView : View("My View") {
    override val root = borderpane {
        setPrefSize(500.0, 300.0)

        center {
            vbox(spacing = 20) {
                alignment = Pos.CENTER

                stackpane {
                    paddingRight = 16

                    imageview("images/icon.png", true)
                }

                label("Đang xử lý...") {
                    style {
                        fontSize = Dimension(18.0, Dimension.LinearUnits.pt)
                    }
                }
            }
        }
    }
}
