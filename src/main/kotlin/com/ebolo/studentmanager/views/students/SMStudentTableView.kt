package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMStudentRefreshEvent
import com.ebolo.studentmanager.services.SMStudentRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import javafx.geometry.Pos
import javafx.stage.Modality
import tornadofx.*

class SMStudentTableView : View() {

    override val root = borderpane {
        top = hbox(spacing = 20, alignment = Pos.CENTER_LEFT) {
            paddingAll = 20

            label("Tìm kiếm")
            textfield()
            button("Tạo mới") {
                action {
                    find<SMStudentInfoView>("mode" to SMCRUDUtils.CRUDMode.NEW)
                        .openModal(modality = Modality.WINDOW_MODAL, block = true)
                }
            }
        }

        center = tableview<SMStudentModel.SMStudentDto> {
            readonlyColumn("Tên", SMStudentModel.SMStudentDto::firstName)
            readonlyColumn("Họ", SMStudentModel.SMStudentDto::lastName)
            readonlyColumn("Nickname", SMStudentModel.SMStudentDto::nickname)
            readonlyColumn("Sinh nhật", SMStudentModel.SMStudentDto::birthday)
            readonlyColumn("Học vấn", SMStudentModel.SMStudentDto::educationLevel) {
                cellFormat { text = it.title }
            }
            readonlyColumn("Số điện thoại", SMStudentModel.SMStudentDto::phone)

            smartResize()

            // subscribe to the refresh event to reset the list
            subscribe<SMStudentRefreshEvent> { event ->
                items.setAll(event.students)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        fire(SMStudentRefreshRequest)
    }
}