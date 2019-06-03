package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.services.SMServiceCentral
import tornadofx.*

/**
 * This view serves as a splash screen to determine either the login view or the main view to be shown
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMSplashView : Fragment("Student Manager") {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        setPrefSize(500.0, 300.0)
    }

    override fun onDock() {
        runAsync {
            serviceCentral.userService.checkCurrentUserAuthentication()
        } ui { authenticated ->
            if (!authenticated) {
                replaceWith<SMLoginFormView>(sizeToScene = true, centerOnScreen = true)
            } else {
                primaryStage.isMaximized = true
                replaceWith<SMMainView>(sizeToScene = true, centerOnScreen = true)
            }
        }
    }
}
