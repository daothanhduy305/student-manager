package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.views.classes.SMClassTableView
import com.ebolo.studentmanager.views.students.SMStudentTableView
import com.ebolo.studentmanager.views.subjects.SMSubjectTableView
import com.ebolo.studentmanager.views.teachers.SMTeacherTableView
import javafx.application.Platform
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
                    item("Quit") {
                        action {
                            Platform.exit()
                            System.exit(0)
                        }
                    }
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

                item("Lớp học", expanded = true) {
                    borderpane {
                        center = classTableView.root
                    }
                }

                item("Học sinh") {
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