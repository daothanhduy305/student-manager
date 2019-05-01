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
class SMSplashView : View() {
    private val serviceCentral: SMServiceCentral by di()

    private val loginView: SMLoginFormView by inject()
    private val mainView: SMMainView by inject()

    override val root = if (
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
                mainView.root
            } else {
                serviceCentral.cacheService.removeSettings(
                    Settings.CREDENTIAL_USERNAME,
                    Settings.CREDENTIAL_PASSWORD,
                    Settings.REMEMBER_CREDENTIAL
                )

                loginView.root
            }
        } else {
            serviceCentral.cacheService.removeSettings(Settings.REMEMBER_CREDENTIAL)

            loginView.root
        }
    } else loginView.root
}
