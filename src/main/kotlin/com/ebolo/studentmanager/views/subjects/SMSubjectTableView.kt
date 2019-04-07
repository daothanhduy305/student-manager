package com.ebolo.studentmanager.views.subjects

import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMSubjectRefreshEvent
import com.ebolo.studentmanager.services.SMSubjectRefreshRequest
import javafx.geometry.Pos
import javafx.stage.Modality
import tornadofx.*

class SMSubjectTableView : View() {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        top = hbox(spacing = 20, alignment = Pos.CENTER_LEFT) {
            paddingAll = 20

            label("Tìm kiếm")
            textfield()
            button("Tạo mới") {
                action {
                    find<SMSubjectInfoFragment>().openModal(modality = Modality.WINDOW_MODAL, block = true)
                }
            }
        }

        center = tableview<SMSubjectModel.SMSubjectDto> {
            readonlyColumn("Tên môn học", SMSubjectModel.SMSubjectDto::name)

            subscribe<SMSubjectRefreshEvent> { event ->
                items.setAll(event.subjects)
            }

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
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMSubjectRefreshRequest)
    }
}