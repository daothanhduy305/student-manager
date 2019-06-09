package com.ebolo.studentmanager.views.teachers

import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.services.SMDataProcessRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTeacherRefreshEvent
import com.ebolo.studentmanager.services.SMTeacherRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.apache.commons.lang3.StringUtils
import tornadofx.*

class SMTeacherTableFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()

    private val teacherList: ObservableList<SMTeacherModel.SMTeacherDto> = FXCollections.observableArrayList()
    private val filteredTeacherList: FilteredList<SMTeacherModel.SMTeacherDto> = FilteredList(teacherList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm giáo viên").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true
                        paddingVertical = 15
                        paddingHorizontal = 30

                        action {
                            find<SMTeacherInfoFragment>(
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

                            filteredTeacherList.setPredicate { studentDto ->
                                tokens.isEmpty() || tokens.any {
                                    StringUtils.stripAccents(studentDto.firstName).toLowerCase().contains(it)
                                        || StringUtils.stripAccents(studentDto.lastName).toLowerCase().contains(it)
                                }
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMTeacherModel.SMTeacherDto>(filteredTeacherList) {
                multiSelect()

                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }.contentWidth(padding = 30.0)

                readonlyColumn("Họ", SMTeacherModel.SMTeacherDto::lastName).weightedWidth(1, minContentWidth = true, padding = 20.0)

                readonlyColumn("Tên", SMTeacherModel.SMTeacherDto::firstName).weightedWidth(1, minContentWidth = true, padding = 20.0)

                readonlyColumn("Ngày sinh", SMTeacherModel.SMTeacherDto::birthday).remainingWidth()

                setOnMouseClicked {
                    if (it.clickCount == 2) {
                        find<SMTeacherInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "teacherModel" to SMTeacherModel(selectedItem)
                        ).openModal()
                    }
                }

                // set up the context menu
                contextmenu {
                    item("Sửa...").action {
                        find<SMTeacherInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "teacherModel" to SMTeacherModel(selectedItem)
                        ).openModal()
                    }

                    item("Xóa").action {
                        serviceCentral.teacherService.deleteTeachers(selectionModel.selectedItems.map { it.id }.toList())
                        fire(SMDataProcessRequest {
                            fire(SMTeacherRefreshRequest)
                        })
                    }
                }

                smartResize()

                // subscribe to the refresh event to reset the list
                subscribe<SMTeacherRefreshEvent> { event ->
                    runAsync { teacherList.setAll(event.teachers) } ui { requestResize() }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMDataProcessRequest {
            fire(SMTeacherRefreshRequest)
        })
    }
}