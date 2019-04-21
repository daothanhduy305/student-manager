package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.views.classes.SMClassTableView
import com.ebolo.studentmanager.views.students.SMStudentTableView
import com.ebolo.studentmanager.views.subjects.SMSubjectTableView
import com.ebolo.studentmanager.views.teachers.SMTeacherTableView
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDrawer
import com.jfoenix.controls.JFXHamburger
import com.jfoenix.controls.JFXToolbar
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import tornadofx.*


class SMMainView : View("StuMan v0.0.1-SNAPSHOT") {
    private val subjectTableView: SMSubjectTableView by inject()
    private val studentTableView: SMStudentTableView by inject()
    private val classTableView: SMClassTableView by inject()
    private val teacherTableView: SMTeacherTableView by inject()

    private var drawer: JFXDrawer by singleAssign()
    private var backTransition: HamburgerSlideCloseTransition by singleAssign()
    private var mainViewPanel: BorderPane by singleAssign()

    private val defaultView = "Lớp học"
    private val toolbarTitle = SimpleStringProperty(defaultView)

    override val root = borderpane {
        top {
            vbox {
                /*menubar {
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
                }*/

                this += JFXToolbar().apply {
                    paddingAll = 20

                    style {
                        backgroundColor += c("#3f51b5")
                    }

                    leftItems += JFXHamburger().apply {
                        backTransition = HamburgerSlideCloseTransition(this).apply {
                            rate = -1.0
                        }

                        setOnMouseClicked {
                            if (drawer.isOpened) {
                                drawer.close()
                            } else {
                                drawer.open()
                            }
                        }
                    }

                    leftItems += Label().apply {
                        bind(toolbarTitle)
                        paddingLeft = 20

                        style {
                            textFill = c("#fff")
                            fontSize = Dimension(24.0, Dimension.LinearUnits.pt)
                        }
                    }
                }
            }
        }

        center {
            drawer = JFXDrawer().apply {
                this.defaultDrawerSize = 300.0
                this.isOverLayVisible = true
                this.isResizableOnDrag = false

                mainViewPanel = BorderPane().apply {
                    center = when (defaultView) {
                        "Môn học" -> subjectTableView.root
                        "Lớp học" -> classTableView.root
                        "Học sinh" -> studentTableView.root
                        "Giáo viên" -> teacherTableView.root
                        else -> null
                    }
                }

                this.setContent(mainViewPanel)

                this.setSidePane(VBox().apply {
                    val menuButtons: MutableList<Pair<String, JFXButton>> = mutableListOf()

                    /**
                     * Method to build menu buttons for this drawer
                     *
                     * @author ebolo
                     *
                     * @param title String
                     * @param representingRoot Node
                     */
                    fun addMenuButton(title: String, representingRoot: Node) {
                        val button = JFXButton(title).apply {
                            prefWidth = 300.0
                            prefHeight = 60.0

                            style {
                                alignment = Pos.CENTER_LEFT
                                fontSize = Dimension(10.0, Dimension.LinearUnits.pt)

                                if (title == defaultView) {
                                    backgroundColor += c("#ddd")
                                }
                            }

                            action {
                                mainViewPanel.center = representingRoot

                                menuButtons.filter { it.first != title }.forEach {
                                    it.second.style {
                                        alignment = Pos.CENTER_LEFT
                                        fontSize = Dimension(10.0, Dimension.LinearUnits.pt)
                                    }
                                }

                                style {
                                    alignment = Pos.CENTER_LEFT
                                    fontSize = Dimension(10.0, Dimension.LinearUnits.pt)
                                    backgroundColor += c("#ddd")
                                }
                                toolbarTitle.value = title
                                drawer.close()
                            }
                        }

                        menuButtons.add(title to button)
                        this += button
                    }

                    addMenuButton("Môn học", subjectTableView.root)
                    addMenuButton("Lớp học", classTableView.root)
                    addMenuButton("Học sinh", studentTableView.root)
                    addMenuButton("Giáo viên", teacherTableView.root)
                })

                setOnDrawerClosing {
                    backTransition.rate = -1.0
                    backTransition.play()
                }

                setOnDrawerOpening {
                    backTransition.rate = 1.0
                    backTransition.play()
                }
            }

            this += drawer
        }
    }
}