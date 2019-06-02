package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.views.setup.SMSetupResultView
import tornadofx.*

/**
 * Dummy view to be loaded initially while waiting for the spring eco system to be loaded
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMInitView : View("Loading...") {
    override val root = borderpane {
        setPrefSize(500.0, 300.0)
    }

    override fun onDock() {
        runAsync {
            (app as StudentManagerApplication).setupApp()
        } ui {
            if (it.success) {
                replaceWith<SMSplashView>(sizeToScene = true, centerOnScreen = true)
            } else {
                replaceWith<SMSetupResultView>(sizeToScene = true, centerOnScreen = true)
            }
        }
    }
}
