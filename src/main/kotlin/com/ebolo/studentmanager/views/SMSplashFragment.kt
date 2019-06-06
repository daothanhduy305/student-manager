package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.services.SMServiceCentral
import tornadofx.Fragment
import tornadofx.borderpane

/**
 * This view serves as a splash screen to determine either the login view or the main view to be shown
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMSplashFragment : Fragment("Student Manager") {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        setPrefSize(500.0, 300.0)
    }

    override fun onDock() {
        runAsync {
            serviceCentral.userService.checkCurrentUserAuthentication()
        } ui { authenticated ->
            if (!authenticated) {
                replaceWith<SMLoginFormFragment>(sizeToScene = true, centerOnScreen = true)
            } else {
                primaryStage.isMaximized = true
                replaceWith<SMMainFragment>(sizeToScene = true, centerOnScreen = true)
            }
        }
    }
}
