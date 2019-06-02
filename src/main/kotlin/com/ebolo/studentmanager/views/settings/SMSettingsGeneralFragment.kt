package com.ebolo.studentmanager.views.settings

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.services.SMRestartAppRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.Settings
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class SMSettingsGeneralFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()
    private val databaseUriProperty = SimpleStringProperty("")
    private val databaseNameProperty = SimpleStringProperty("")

    override val root = form {
        vbox {
            vgrow = Priority.ALWAYS
            paddingAll = 10

            hbox {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                vbox {
                    hgrow = Priority.ALWAYS

                    fieldset(labelPosition = Orientation.VERTICAL, text = "Dữ liệu") {
                        spacing = 10.0

                        field("Tên cơ sở dữ liệu") {
                            this += JFXTextField().apply {
                                bind(databaseNameProperty)
                            }
                        }

                        field("Thông tin kết nối") {
                            this += JFXTextField().apply {
                                bind(databaseUriProperty)
                            }
                        }
                    }
                }
            }

            hbox(spacing = 20) {
                alignment = Pos.CENTER_RIGHT

                this += JFXButton("Hủy bỏ").apply {
                    useMaxWidth = true
                    buttonType = JFXButton.ButtonType.RAISED
                    paddingVertical = 15
                    paddingHorizontal = 30

                    style {
                        backgroundColor += c("#ff5533")
                        textFill = c("#fff")
                    }

                    action {
                        close()
                    }
                }

                this += JFXButton("Hoàn tất").apply {
                    useMaxWidth = true
                    buttonType = JFXButton.ButtonType.RAISED
                    paddingVertical = 15
                    paddingHorizontal = 30

                    style {
                        backgroundColor += c("#fff")
                    }

                    action {
                        close()
                        val messageView = find<SMApplyingSettingsView>()

                        val dbUri = databaseUriProperty.value
                        val dbName = databaseNameProperty.value

                        if (dbUri.isNotBlank() && dbName.isNotBlank()) {
                            // Set the new connection string and refresh the context
                            runAsync {
                                StudentManagerApplication.setSettings(
                                    Settings.DATABASE_NAME to dbName,
                                    Settings.DATABASE_URI to dbUri
                                )

                                with(app as StudentManagerApplication) {
                                    if (context != null && context!!.isActive) context!!.close()
                                    setupApp()
                                }
                            } ui {
                                messageView.close()
                                fire(SMRestartAppRequest)
                            }

                            messageView.openModal(
                                block = true, resizable = false, escapeClosesWindow = false)
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()

        val mongoDbName = StudentManagerApplication.getSetting(Settings.DATABASE_NAME) as String?
        if (mongoDbName != null) {
            databaseNameProperty.value = mongoDbName
        }

        val mongoDbUri = StudentManagerApplication.getSetting(Settings.DATABASE_URI) as String?
        if (mongoDbUri != null) {
            databaseUriProperty.value = mongoDbUri
        }
    }
}
