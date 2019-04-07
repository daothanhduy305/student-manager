package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.services.SMClassRefreshEvent
import com.ebolo.studentmanager.services.SMClassRefreshRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.utils.SMCRUDUtils
import javafx.geometry.Pos
import javafx.stage.Modality
import tornadofx.*

class SMClassTableView : View() {
    private val serviceCentral: SMServiceCentral by di()
    private val classModel: SMClassModel by inject()

    override val root = borderpane {
        top = hbox(spacing = 20, alignment = Pos.CENTER_LEFT) {
            paddingAll = 20

            label("Tìm kiếm")
            textfield()
            button("Tạo mới") {
                action {
                    find<SMClassInfoView>("mode" to SMCRUDUtils.CRUDMode.NEW)
                        .openModal(modality = Modality.WINDOW_MODAL, block = true)
                }
            }
        }

        center = tableview<SMClassModel.SMClassDto> {
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
                    find<SMClassInfoView>("mode" to SMCRUDUtils.CRUDMode.EDIT)
                        .apply { classModel.item = selectedItem ?: SMClassModel.SMClassDto() }
                        .openModal(modality = Modality.WINDOW_MODAL, block = true)
                }

                item("Xóa").action {
                    /*if (selectedItem != null) runAsync {
                        serviceCentral.studentService.deleteStudent(selectedItem!!.id)
                        fire(SMStudentRefreshRequest)
                    }*/
                }
            }

            // subscribe to the refresh event to reset the list
            subscribe<SMClassRefreshEvent> { event ->
                items.setAll(event.classes)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMClassRefreshRequest)
    }
}