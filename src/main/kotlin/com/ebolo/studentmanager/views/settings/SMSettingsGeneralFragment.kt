package com.ebolo.studentmanager.views.settings

import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.services.SMGlobal
import com.ebolo.studentmanager.services.SMTheme
import com.ebolo.studentmanager.services.Settings
import com.ebolo.studentmanager.utils.formatDecimal
import com.ebolo.studentmanager.utils.isFormattedLong
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleLongProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.TextFormatter
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import tornadofx.*

class SMSettingsGeneralFragment : Fragment() {
    private val logger = loggerFor(this::class.java)

    private val useBackgroundSync = SimpleBooleanProperty(
        {
            var savedUseBgSync = false

            preferences {
                savedUseBgSync = getBoolean(Settings.USE_BACGROUND_SYNC, false)
            }

            savedUseBgSync
        }()
    )

    private val syncIntervalModel = ViewModel()
    private val syncInterval = syncIntervalModel.bind {
        SimpleLongProperty(
            {
                var savedSyncInterval = 0L

                preferences {
                    savedSyncInterval = getLong(Settings.SYNC_INTERVAL, SMGlobal.DEFAULT_SYNC_INTERVAL)
                }

                savedSyncInterval
            }()
        )
    }

    private val oldSyncInterval = {
        var savedSyncInterval = 0L

        preferences {
            savedSyncInterval = getLong(Settings.SYNC_INTERVAL, SMGlobal.DEFAULT_SYNC_INTERVAL)
        }

        savedSyncInterval
    }()

    override val root = form {
        vbox {
            vgrow = Priority.ALWAYS
            paddingAll = 10

            hbox {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                vbox(spacing = 20) {
                    hgrow = Priority.ALWAYS

                    fieldset(labelPosition = Orientation.HORIZONTAL, text = "Đồng bộ dữ liệu") {
                        spacing = 10.0

                        hbox(spacing = 10) {
                            field("Đồng bộ dữ liệu nền") {
                                this += JFXCheckBox().apply {
                                    bind(useBackgroundSync)
                                }
                            }

                            field("Đồng bộ sau mỗi") {
                                hbox(spacing = 5) {
                                    this += JFXTextField().apply {
                                        textFormatter = TextFormatter(object : StringConverter<Number?>() {
                                            override fun fromString(string: String?): Number? {
                                                return if (string != null && string.isFormattedLong())
                                                    string.trim().replace("[^\\d]".toRegex(), "").toLong()
                                                else null
                                            }

                                            override fun toString(number: Number?): String {
                                                return if (number != null) return number.toLong().toString().formatDecimal()
                                                else ""
                                            }
                                        })

                                        textProperty().onChange {
                                            runLater { commitValue() }
                                        }

                                        bind(syncInterval)

                                        validator { text ->
                                            when {
                                                !isDisabled && text.isNullOrBlank() -> error("This field is required")
                                                !isDisabled && !text.isNullOrBlank() && !text.isFormattedLong() -> error("Number is required")
                                                else -> null
                                            }
                                        }

                                        enableWhen(useBackgroundSync)
                                    }

                                    label("(ms)") {
                                        style {
                                            fontSize = Dimension(12.0, Dimension.LinearUnits.pt)
                                        }
                                    }
                                }
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
                        backgroundColor += c(SMTheme.CANCEL_BUTTON_COLOR)
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
                        preferences {
                            putBoolean(Settings.USE_BACGROUND_SYNC, useBackgroundSync.value)

                            if (syncIntervalModel.validate()) {
                                putLong(Settings.SYNC_INTERVAL, syncInterval.value as Long)
                            }
                        }

                        close()
                    }
                }
            }
        }
    }
}
