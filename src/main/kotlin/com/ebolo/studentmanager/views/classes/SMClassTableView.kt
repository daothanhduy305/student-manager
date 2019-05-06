package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.services.SMClassListRefreshEvent
import com.ebolo.studentmanager.services.SMClassListRefreshRequest
import com.ebolo.studentmanager.services.SMServiceCentral
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

class SMClassTableView : View() {
    private val serviceCentral: SMServiceCentral by di()

    private val classList: ObservableList<SMClassModel.SMClassDto> = FXCollections.observableArrayList()
    private val filteredClassList: FilteredList<SMClassModel.SMClassDto> = FilteredList(classList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm lớp").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        isDisableVisualFocus = true

                        action {
                            find<SMClassInfoFragment>(
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
                            val textToSearch = StringUtils.stripAccents(this.text).toLowerCase()

                            filteredClassList.setPredicate { classDto ->
                                StringUtils.stripAccents(classDto.name).toLowerCase().contains(textToSearch)
                                    || StringUtils.stripAccents(classDto.subject.name).toLowerCase().contains(textToSearch)
                                    || StringUtils.stripAccents(classDto.teacher.firstName).toLowerCase().contains(textToSearch)
                                    || StringUtils.stripAccents(classDto.teacher.lastName).toLowerCase().contains(textToSearch)
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMClassModel.SMClassDto>(filteredClassList) {
                multiSelect()

                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }

                readonlyColumn("Tên lớp", SMClassModel.SMClassDto::name)
                readonlyColumn("Giáo viên", SMClassModel.SMClassDto::teacher) {
                    cellFormat { teacher -> text = "${teacher.lastName} ${teacher.firstName}" }
                }
                readonlyColumn("Môn", SMClassModel.SMClassDto::subject) {
                    cellFormat { subject -> text = subject.name }
                }

                smartResize()

                // set up the context menu
                contextmenu {
                    item("Sửa...").action {
                        find<SMClassInfoFragment>(
                            "mode" to SMCRUDUtils.CRUDMode.EDIT,
                            "classModel" to SMClassModel(selectedItem)
                        ).openModal()
                    }

                    item("Xóa").action {
                        serviceCentral.classService.deleteClasses(selectionModel.selectedItems.map { it.id }.toList())
                        fire(SMClassListRefreshRequest)
                    }
                }

                // subscribe to the refresh event to reset the list
                subscribe<SMClassListRefreshEvent> { event ->
                    searchBox.text = ""
                    runAsync { classList.setAll(event.classes) } ui {
                        requestResize()
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMClassListRefreshRequest)
    }
}