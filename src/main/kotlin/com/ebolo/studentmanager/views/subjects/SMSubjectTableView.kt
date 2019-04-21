package com.ebolo.studentmanager.views.subjects

import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMSubjectRefreshEvent
import com.ebolo.studentmanager.services.SMSubjectRefreshRequest
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.stage.Modality
import tornadofx.*

class SMSubjectTableView : View() {
    private val serviceCentral: SMServiceCentral by di()

    private val subjectList: ObservableList<SMSubjectModel.SMSubjectDto> = FXCollections.observableArrayList()
    private val filteredSubjectList: FilteredList<SMSubjectModel.SMSubjectDto> = FilteredList(subjectList)

    private var searchBox by singleAssign<JFXTextField>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 20

                // Action buttons
                hbox {
                    alignment = Pos.CENTER_LEFT
                    hgrow = Priority.ALWAYS

                    this += JFXButton("Thêm môn học").apply {
                        buttonType = JFXButton.ButtonType.RAISED

                        action {
                            find<SMSubjectInfoFragment>().openModal(modality = Modality.WINDOW_MODAL, block = true)
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
                            filteredSubjectList.setPredicate { subjectDto ->
                                subjectDto.name.toLowerCase().contains(this.text.toLowerCase())
                            }
                        }
                    }

                    this += searchBox
                }
            }
        }

        center {
            tableview<SMSubjectModel.SMSubjectDto>(filteredSubjectList) {
                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }

                readonlyColumn("Tên môn học", SMSubjectModel.SMSubjectDto::name)

                smartResize()

                contextmenu {
                    item("Sửa...").action {
                        // TODO: implement this action
                    }

                    item("Xóa").action {
                        if (selectedItem != null) runAsync {
                            serviceCentral.subjectService.deleteSubject(selectedItem!!.id)
                            fire(SMSubjectRefreshRequest)
                        }
                    }
                }

                subscribe<SMSubjectRefreshEvent> { event ->
                    runAsync { subjectList.setAll(event.subjects) }.ui {
                        smartResize()
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMSubjectRefreshRequest)
    }
}