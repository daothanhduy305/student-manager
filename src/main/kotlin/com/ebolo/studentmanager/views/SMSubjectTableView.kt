package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.services.SMServiceCentral
import javafx.geometry.Insets
import javafx.geometry.Pos
import tornadofx.*

class SMSubjectTableView : View() {
    private val serviceCentral: SMServiceCentral by di()

    override val root = borderpane {
        top = hbox(spacing = 20, alignment = Pos.CENTER_LEFT) {
            padding = Insets(20.0)

            label("Tìm kiếm")
            textfield()
            button("Tạo mới")
        }

        center = tableview<SMSubjectModel> {
            readonlyColumn("Tên môn học", SMSubjectModel::name)
        }
    }
}