package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.services.SMGlobal.APP_NAME
import com.ebolo.studentmanager.services.SMServiceCentral
import javafx.geometry.Pos
import tornadofx.*

/**
 * This view serves as a splash screen to determine either the login view or the main view to be shown
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMSplashFragment : Fragment(APP_NAME) {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        setPrefSize(500.0, 300.0)

        center {
            vbox(spacing = 20) {
                alignment = Pos.CENTER

                stackpane {
                    paddingRight = 16

                    imageview("images/icon.png", true)
                }

                label("Hoàn tất") {
                    style {
                        fontSize = Dimension(18.0, Dimension.LinearUnits.pt)
                    }
                }
            }
        }
    }

    override fun onDock() {
        runAsync {
            serviceCentral.userService.checkCurrentUserAuthentication()
        } ui { authenticated ->
            if (!authenticated) {
                replaceWith<SMLoginFormFragment>(sizeToScene = true, centerOnScreen = true)
            } else {
                primaryStage.isMaximized = false
                primaryStage.isMaximized = true
                replaceWith<SMMainFragment>(centerOnScreen = true)
            }
        }
    }
}
