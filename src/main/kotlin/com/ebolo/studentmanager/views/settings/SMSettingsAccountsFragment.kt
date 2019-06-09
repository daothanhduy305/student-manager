package com.ebolo.studentmanager.views.settings

import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMUserListRefreshEvent
import com.ebolo.studentmanager.services.SMUserListRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.ebolo.studentmanager.views.utils.ui.SMConfirmDialog
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.apache.commons.lang3.StringUtils
import tornadofx.*

class SMSettingsAccountsFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()

    private val userList: ObservableList<SMUserModel.SMUserDto> = FXCollections.observableArrayList()
    private val filteredUserList: FilteredList<SMUserModel.SMUserDto> = FilteredList(userList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        minWidth = 500.0
        prefWidth = 500.0

        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm người dùng").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true
                        paddingVertical = 15
                        paddingHorizontal = 30

                        action {
                            find<SMAddUserFragment>(
                                "mode" to SMCRUDUtils.CRUDMode.NEW
                            ).openModal()
                        }

                        style {
                            backgroundColor += c("#ffffff")
                        }
                    }
                }

                // Search box and misc
                hbox {
                    alignment = Pos.CENTER_RIGHT
                    hgrow = Priority.ALWAYS

                    searchBox = JFXTextField().apply {
                        promptText = "Tìm kiếm"

                        textProperty().addListener { _, _, _ ->
                            val tokens = this.text
                                .split(' ')
                                .filter { it.isNotBlank() }
                                .map { StringUtils.stripAccents(it).toLowerCase() }

                            filteredUserList.setPredicate { userDto ->
                                tokens.isEmpty() || tokens.any {
                                    StringUtils.stripAccents(userDto.username).toLowerCase().contains(it)
                                }
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMUserModel.SMUserDto>(filteredUserList) {
                multiSelect()

                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }

                readonlyColumn("Tên đăng nhập", SMUserModel.SMUserDto::username)

                smartResize()

                // subscribe to the refresh event to reset the list
                subscribe<SMUserListRefreshEvent> { event ->
                    searchBox.text = ""
                    runAsync { userList.setAll(event.users) } ui {
                        requestResize()
                    }
                }

                contextmenu {
                    /*item("Sửa...").action {
                        find<SMClassInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "classModel" to SMClassModel(selectedItem)
                        ).openModal()
                    }*/

                    item("Xóa").action {
                        find<SMConfirmDialog>(
                            "dialogContent" to "Tiếp tục xóa?",
                            "onOKClicked" to {
                                runAsync {
                                    serviceCentral.userService.deleteUsers(selectionModel.selectedItems.map { it.id }.toList())
                                } ui {
                                    if (it.success) {
                                        fire(SMUserListRefreshRequest)
                                    }
                                }
                            }
                        ).openModal()
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMUserListRefreshRequest)
    }
}
