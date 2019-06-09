package com.ebolo.studentmanager.views.settings

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.services.SMRestartAppRequest
import com.ebolo.studentmanager.services.Settings
import com.ebolo.studentmanager.views.utils.ui.SMErrorsDialog
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import tornadofx.*

class SMSettingsGeneralFragment : Fragment() {
    private val logger = loggerFor(this::class.java)

    private val databaseModel = ViewModel()
    private val databaseUriProperty = databaseModel.bind {
        SimpleStringProperty({
            StudentManagerApplication.getSetting(Settings.DATABASE_URI) as String? ?: ""
        }())
    }
    private val databaseNameProperty = databaseModel.bind {
        SimpleStringProperty({
            StudentManagerApplication.getSetting(Settings.DATABASE_NAME) as String? ?: ""
        }())
    }

    private val masterAccountModel = ViewModel()
    private val masterAccountUsernameProperty = masterAccountModel.bind {
        SimpleStringProperty({
            StudentManagerApplication.getSetting(Settings.MASTER_ACCOUNT_USERNAME) as String? ?: ""
        }())
    }
    private val masterAccountPasswordProperty = masterAccountModel.bind { SimpleStringProperty("DummyPassword") }

    private val oldDBName = StudentManagerApplication.getSetting(Settings.DATABASE_NAME) as String? ?: ""
    private val oldDBConnectionString = StudentManagerApplication.getSetting(Settings.DATABASE_URI) as String? ?: ""

    override val root = form {
        vbox {
            vgrow = Priority.ALWAYS
            paddingAll = 10

            hbox {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                vbox(spacing = 20) {
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

                    fieldset(labelPosition = Orientation.VERTICAL, text = "Tài khoản chủ") {
                        spacing = 10.0

                        field("Tên đăng nhập") {
                            this += JFXTextField().apply {
                                bind(masterAccountUsernameProperty)
                            }
                        }

                        field("Mật khẩu") {
                            this += JFXPasswordField().apply {
                                bind(masterAccountPasswordProperty)
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

                    enableWhen((databaseModel.valid.and(databaseModel.dirty)
                        .or(masterAccountModel.valid.and(masterAccountModel.dirty)))
                        .and(masterAccountModel.valid.and(databaseModel.valid)))

                    action {
                        val messageView = find<SMApplyingSettingsView>()

                        runAsync {
                            var result = true
                            val errorList = mutableListOf<StudentManagerApplication.SetupError>()

                            if (masterAccountModel.isDirty) {
                                StudentManagerApplication.setSettings(
                                    Settings.MASTER_ACCOUNT_USERNAME to masterAccountUsernameProperty.value,
                                    Settings.MASTER_ACCOUNT_PASSWORD to BCryptPasswordEncoder().encode(masterAccountPasswordProperty.value)
                                )
                            }

                            if (databaseModel.isDirty) {
                                StudentManagerApplication.setSettings(
                                    Settings.DATABASE_NAME to databaseNameProperty.value,
                                    Settings.DATABASE_URI to databaseUriProperty.value
                                )

                                try {
                                    (app as StudentManagerApplication).setupApp()
                                } catch (e: Exception) {
                                    logger.error("Could not setup the database connection. Falling back to the old info...", e)

                                    StudentManagerApplication.setSettings(
                                        Settings.DATABASE_NAME to oldDBName,
                                        Settings.DATABASE_URI to oldDBConnectionString
                                    )

                                    databaseNameProperty.value = oldDBName
                                    databaseUriProperty.value = oldDBConnectionString

                                    (app as StudentManagerApplication).setupApp()

                                    result = false
                                    errorList.add(StudentManagerApplication.SetupError.DATABASE_ERROR)
                                }
                            }

                            StudentManagerApplication.SetupResult(result, errorList)
                        } ui { result ->
                            messageView.close()

                            if (result.success) {
                                close()
                                if (databaseModel.isDirty) fire(SMRestartAppRequest)
                            } else {
                                find<SMErrorsDialog>(
                                    "dialogTitle" to "Đã xảy ra lỗi:",
                                    "errorList" to result.errors.map { it.friendlyMessage }
                                ).openModal()
                            }
                        }

                        messageView.openModal(
                            block = true, resizable = false, escapeClosesWindow = false)
                    }
                }
            }
        }
    }
}
