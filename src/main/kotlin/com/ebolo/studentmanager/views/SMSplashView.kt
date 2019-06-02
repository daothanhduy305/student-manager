package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.entities.SMUserEntity
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.Settings
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
class SMSplashView : Fragment("Student Manager") {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        setPrefSize(500.0, 300.0)
    }

    override fun onDock() {
        runAsync {
            var showLogin = true
            if (StudentManagerApplication.getSetting(Settings.REMEMBER_CREDENTIAL, false) as Boolean) {
                // If the remember has been ticked then check the saved credential
                if (StudentManagerApplication.hasSetting(Settings.CREDENTIAL_USERNAME)
                    && StudentManagerApplication.hasSetting(Settings.CREDENTIAL_PASSWORD)) {

                    val username = StudentManagerApplication.getSetting(Settings.CREDENTIAL_USERNAME) as String
                    val hashedPassword = StudentManagerApplication.getSetting(Settings.CREDENTIAL_PASSWORD) as String

                    if (serviceCentral.userService.login(SMUserEntity().apply {
                            this.username = username
                            this.password = hashedPassword
                        })) {

                        showLogin = false
                    } else {
                        StudentManagerApplication.removeSettings(
                            Settings.CREDENTIAL_USERNAME,
                            Settings.CREDENTIAL_PASSWORD,
                            Settings.REMEMBER_CREDENTIAL
                        )
                    }
                } else {
                    StudentManagerApplication.removeSettings(Settings.REMEMBER_CREDENTIAL)
                }
            }

            showLogin
        } ui { isShowingLogin ->
            if (isShowingLogin) {
                replaceWith<SMLoginFormView>(sizeToScene = true, centerOnScreen = true)
            } else {
                primaryStage.isMaximized = true
                replaceWith<SMMainView>(sizeToScene = true, centerOnScreen = true)
            }
        }
    }
}
