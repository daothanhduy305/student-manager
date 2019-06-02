package com.ebolo.studentmanager.views.settings

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
            label("Applying settings...")
        }
    }
}
