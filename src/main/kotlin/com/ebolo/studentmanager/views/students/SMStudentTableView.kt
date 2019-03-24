package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.models.SMStudentModel
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
                    find<SMStudentInfoView>().openModal(modality = Modality.WINDOW_MODAL, block = true)
                }
            }
        }

        center = tableview<SMStudentModel> {
            readonlyColumn("Tên", SMStudentModel::firstName)
            readonlyColumn("Họ", SMStudentModel::lastName)
        }
    }
}