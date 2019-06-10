package com.ebolo.studentmanager.views.setup

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.services.SMGlobal.APP_NAME
import com.ebolo.studentmanager.services.Settings
import com.ebolo.studentmanager.views.SMInitFragment
import com.ebolo.studentmanager.views.settings.SMApplyingSettingsView
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import tornadofx.*

/**
 * Fragment to show to configure the vital parts of the system
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property model ViewModel
 * @property databaseNameProperty Property<(kotlin.String..kotlin.String?)>
 * @property databaseUriProperty Property<(kotlin.String..kotlin.String?)>
 * @property masterAccountUsernameProperty Property<(kotlin.String..kotlin.String?)>
 * @property masterAccountPasswordProperty Property<(kotlin.String..kotlin.String?)>
 * @property root BorderPane
 */
class SMSetupFragment : Fragment(APP_NAME) {
    private val model = ViewModel()

    private val databaseNameProperty = model.bind { SimpleStringProperty() }
    private val databaseUriProperty = model.bind { SimpleStringProperty() }

    private val masterAccountUsernameProperty = model.bind { SimpleStringProperty() }
    private val masterAccountPasswordProperty = model.bind { SimpleStringProperty() }

    override val root = borderpane {
        prefWidth = 500.0
        paddingAll = 20

        center {
            vbox {
                form {
                    vbox(spacing = 20) {
                        fieldset(labelPosition = Orientation.VERTICAL, text = "Cơ sở dữ liệu") {
                            field("Tên cơ sở dữ liệu *") {
                                this += JFXTextField().apply {
                                    bind(databaseNameProperty)
                                    required()
                                }
                            }

                            field("Thông tin kết nối *") {
                                this += JFXTextField().apply {
                                    bind(databaseUriProperty)
                                    required()
                                }
                            }
                        }

                        fieldset(labelPosition = Orientation.VERTICAL, text = "Tài khoản chủ") {
                            field("Tên đăng nhập *") {
                                this += JFXTextField().apply {
                                    bind(masterAccountUsernameProperty)
                                    required()
                                }
                            }

                            field("Mật khẩu *") {
                                this += JFXPasswordField().apply {
                                    bind(masterAccountPasswordProperty)
                                    required()
                                }
                            }
                        }
                    }
                }

                hbox(spacing = 20) {
                    alignment = Pos.CENTER_RIGHT

                    this += JFXButton("Hoàn tất").apply {
                        useMaxWidth = true
                        buttonType = JFXButton.ButtonType.RAISED
                        paddingVertical = 15
                        paddingHorizontal = 30

                        style {
                            backgroundColor += c("#fff")
                        }

                        enableWhen(model.valid)

                        action {
                            val messageView = find<SMApplyingSettingsView>()

                            val dbUri = databaseUriProperty.value
                            val dbName = databaseNameProperty.value

                            val masterAccountUsername = masterAccountUsernameProperty.value
                            val masterAccountPassword = masterAccountPasswordProperty.value

                            runAsync {
                                preferences {
                                    put(Settings.DATABASE_NAME, dbName)
                                    put(Settings.DATABASE_URI, dbUri)

                                    put(Settings.MASTER_ACCOUNT_USERNAME, masterAccountUsername)
                                    put(Settings.MASTER_ACCOUNT_PASSWORD, BCryptPasswordEncoder().encode(masterAccountPassword))

                                    putBoolean(Settings.DATABASE_SETUP, true)
                                    putBoolean(Settings.MASTER_ACCOUNT_SETUP, true)
                                }

                                StudentManagerApplication.dbName = dbName
                                StudentManagerApplication.dbUri = dbUri
                            } ui {
                                messageView.close()
                                replaceWith<SMInitFragment>()
                            }

                            messageView.openModal(
                                block = true, resizable = false, escapeClosesWindow = false)
                        }
                    }
                }
            }
        }
    }
}
