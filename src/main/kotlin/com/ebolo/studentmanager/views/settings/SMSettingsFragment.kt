package com.ebolo.studentmanager.views.settings

import com.jfoenix.controls.JFXTabPane
import javafx.scene.control.TabPane
import tornadofx.*

class SMSettingsFragment : Fragment("Cài đặt") {
    override val root = stackpane {
        style {
            backgroundColor += c("#fff")
        }

        this += JFXTabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab("Cài đặt chung") {
                borderpane {
                    center {
                        this += find<SMSettingsGeneralFragment>().root
                    }
                }
            }

            tab("Cài đặt quản trị") {
                borderpane {
                    center {
                        this += find<SMSettingsAdminFragment>().root
                    }
                }
            }

            tab("Tài khoản") {
                borderpane {
                    center {
                        this += find<SMSettingsAccountsFragment>().root
                    }
                }
            }
        }
    }
}
