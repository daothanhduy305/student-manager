package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMClassModel
import tornadofx.View
import tornadofx.readonlyColumn
import tornadofx.tableview

class SMClassTableView : View() {

    override val root = tableview<SMClassModel> {
        readonlyColumn("Tên lớp", SMClassModel::name)
        readonlyColumn("Giáo viên", SMClassModel::teacher)
        readonlyColumn("Môn", SMClassModel::subject)
    }
}