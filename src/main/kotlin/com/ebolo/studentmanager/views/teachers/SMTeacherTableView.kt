package com.ebolo.studentmanager.views.teachers

import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTeacherRefreshEvent
import com.ebolo.studentmanager.services.SMTeacherRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import javafx.geometry.Pos
import javafx.stage.Modality
import tornadofx.*

class SMTeacherTableView : View() {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        top = hbox(spacing = 20, alignment = Pos.CENTER_LEFT) {
            paddingAll = 20

            label("Tìm kiếm")
            textfield()
            button("Tạo mới") {
                action {
                    find<SMTeacherInfoFragment>("mode" to SMCRUDUtils.CRUDMode.NEW)
                        .openModal(modality = Modality.WINDOW_MODAL, block = true)
                }
            }
        }

        center = tableview<SMTeacherModel.SMTeacherDto> {
            readonlyColumn("Họ", SMTeacherModel.SMTeacherDto::lastName)
            readonlyColumn("Tên", SMTeacherModel.SMTeacherDto::firstName)
            readonlyColumn("Ngày sinh", SMTeacherModel.SMTeacherDto::birthday)

            smartResize()

            // set up the context menu
            contextmenu {
                item("Sửa...").action {
                    find<SMTeacherInfoFragment>(
                        "mode" to SMCRUDUtils.CRUDMode.EDIT,
                        "teacherModel" to SMTeacherModel(selectedItem)
                    ).openModal(modality = Modality.WINDOW_MODAL, block = true)
                }

                item("Xóa").action {
                    if (selectedItem != null) runAsync {
                        serviceCentral.teacherService.deleteTeacher(selectedItem!!.id)
                        fire(SMTeacherRefreshRequest)
                    }
                }
            }

            // subscribe to the refresh event to reset the list
            subscribe<SMTeacherRefreshEvent> { event ->
                asyncItems { event.teachers }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMTeacherRefreshRequest)
    }
}