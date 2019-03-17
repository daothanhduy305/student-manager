package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMStudentModel
import tornadofx.View
import tornadofx.readonlyColumn
import tornadofx.tableview

class SMStudentTableView : View() {

    override val root = tableview<SMStudentModel> {
        readonlyColumn("Tên", SMStudentModel::firstName)
        readonlyColumn("Họ", SMStudentModel::lastName)
    }
}