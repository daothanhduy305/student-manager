package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.views.setup.SMSetupResultView
import javafx.geometry.Pos
import tornadofx.*

/**
 * Dummy view to be loaded initially while waiting for the spring eco system to be loaded
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMInitFragment : Fragment("Student  Manager") {
    override val root = borderpane {
        setPrefSize(500.0, 300.0)

        center {
            vbox(spacing = 20) {
                alignment = Pos.CENTER

                stackpane {
                    paddingRight = 16

                    imageview("images/icon.png", true)
                }

                label("Đang khởi tạo...") {
                    style {
                        fontSize = Dimension(18.0, Dimension.LinearUnits.pt)
                    }
                }
            }
        }
    }

    override fun onDock() {
        runAsync {
            (app as StudentManagerApplication).setupApp()
        } ui {
            if (it.success) {
                replaceWith<SMSplashFragment>(sizeToScene = true, centerOnScreen = true)
            } else {
                replaceWith<SMSetupResultView>(sizeToScene = true, centerOnScreen = true)
            }
        }
    }
}
