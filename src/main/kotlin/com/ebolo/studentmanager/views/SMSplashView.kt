package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.entities.SMUserEntity
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.Settings
import tornadofx.*

/**
 * This view serves as a splash screen to determine either the login view or the main view to be shown
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property root BorderPane
 */
class SMSplashView : View("Student Manager") {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        setPrefSize(500.0, 300.0)
    }

    override fun onDock() {
        runAsync {
            var showLogin = true
            if (
                serviceCentral.cacheService.cache.containsKey(Settings.REMEMBER_CREDENTIAL)
                && serviceCentral.cacheService.cache[Settings.REMEMBER_CREDENTIAL] as Boolean) {
                // If the remember has been ticked then check the saved credential
                if (serviceCentral.cacheService.cache.containsKey(Settings.CREDENTIAL_USERNAME)
                    && serviceCentral.cacheService.cache.containsKey(Settings.CREDENTIAL_PASSWORD)) {

                    val username = serviceCentral.cacheService.cache[Settings.CREDENTIAL_USERNAME] as String
                    val hashedPassword = serviceCentral.cacheService.cache[Settings.CREDENTIAL_PASSWORD] as String

                    if (serviceCentral.userService.login(SMUserEntity().apply {
                            this.username = username
                            this.password = hashedPassword
                        })) {

                        showLogin = false
                    } else {
                        serviceCentral.cacheService.removeSettings(
                            Settings.CREDENTIAL_USERNAME,
                            Settings.CREDENTIAL_PASSWORD,
                            Settings.REMEMBER_CREDENTIAL
                        )
                    }
                } else {
                    serviceCentral.cacheService.removeSettings(Settings.REMEMBER_CREDENTIAL)
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
