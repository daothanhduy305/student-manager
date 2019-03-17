package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.models.SMSubjectModel
import tornadofx.View
import tornadofx.readonlyColumn
import tornadofx.tableview

class SMSubjectTableView : View() {


    override val root = tableview<SMSubjectModel> {
        readonlyColumn("Tên môn học", SMSubjectModel::name)
    }
}