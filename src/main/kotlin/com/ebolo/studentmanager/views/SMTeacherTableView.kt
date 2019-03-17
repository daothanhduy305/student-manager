package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMTeacherModel
import tornadofx.View
import tornadofx.readonlyColumn
import tornadofx.tableview

class SMTeacherTableView : View() {

    override val root = tableview<SMTeacherModel> {
        readonlyColumn("Họ", SMTeacherModel::lastName)
        readonlyColumn("Tên", SMTeacherModel::firstName)
    }
}