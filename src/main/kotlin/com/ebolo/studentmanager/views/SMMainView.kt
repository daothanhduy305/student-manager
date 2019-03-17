package com.ebolo.studentmanager.views

import tornadofx.*

class SMMainView : View("StuMan v0.0.1-SNAPSHOT") {
    private val subjectTableView: SMSubjectTableView by inject()
    private val studentTableView: SMStudentTableView by inject()
    private val classTableView: SMClassTableView by inject()
    private val teacherTableView: SMTeacherTableView by inject()

    override val root = borderpane {
        top {
            menubar {
                menu("File") {
                    menu("Connect") {
                        item("Facebook")
                        item("Twitter")
                    }
                    item("Save")
                    item("Quit")
                }
                menu("Edit") {
                    item("Copy")
                    item("Paste")
                }
            }
        }

        center {
            drawer {
                item("Môn học") {
                    borderpane {
                        center = subjectTableView.root
                    }
                }

                item("Lớp học") {
                    borderpane {
                        center = classTableView.root
                    }
                }

                item("Học sinh", expanded = true) {
                    borderpane {
                        center = studentTableView.root
                    }
                }

                item("Giáo viên") {
                    borderpane {
                        center = teacherTableView.root
                    }
                }
            }
        }
    }
}