package com.ebolo.studentmanager.views

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.classes.SMClassTableView
import com.ebolo.studentmanager.views.students.SMStudentTableView
import com.ebolo.studentmanager.views.subjects.SMSubjectTableView
import com.ebolo.studentmanager.views.teachers.SMTeacherTableView
import com.jfoenix.controls.*
import com.jfoenix.controls.JFXPopup.PopupHPosition
import com.jfoenix.controls.JFXPopup.PopupVPosition
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*


class SMMainView : View("StuMan v0.0.1-SNAPSHOT") {
    private val logger = loggerFor(SMMainView::class.java)
    private val serviceCentral: SMServiceCentral by di()

    private val subjectTableView: SMSubjectTableView by inject()
    private val studentTableView: SMStudentTableView by inject()
    private val classTableView: SMClassTableView by inject()
    private val teacherTableView: SMTeacherTableView by inject()

    private var drawer: JFXDrawer by singleAssign()
    private var backTransition: HamburgerSlideCloseTransition by singleAssign()
    private var mainViewPanel: BorderPane by singleAssign()

    private val defaultView = "Lớp học"
    private val toolbarTitle = SimpleStringProperty(defaultView)

    override val root = stackpane {
        borderpane {
            top {
                vbox {
                    this += JFXToolbar().apply {
                        paddingAll = 20

                        style {
                            backgroundColor += c("#3f51b5")
                        }

                        // region left
                        leftItems += JFXHamburger().apply {
                            backTransition = HamburgerSlideCloseTransition(this).apply {
                                rate = -1.0
                            }

                            setOnMouseClicked {
                                drawer.toggle()

                                backTransition.rate = if (drawer.isOpened || drawer.isOpening) 1.0 else -1.0

                                backTransition.play()
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
                        // endregion

                        // region right
                        rightItems += JFXRippler().apply menuIcon@{
                            maskType = JFXRippler.RipplerMask.CIRCLE
                            ripplerFill = c("#fff")

                            control = MaterialIconView(MaterialIcon.MORE_VERT).apply {
                                fill = c("#fff")
                                glyphSize = 36

                                setOnMouseClicked {
                                    JFXPopup().apply contextualMenu@{
                                        popupContent = vbox {
                                            /**
                                             * Method to build menu buttons for this drawer
                                             *
                                             * @author ebolo
                                             *
                                             * @param title String
                                             */
                                            fun addMenuButton(title: String, actionHandler: (() -> Unit)? = null) {
                                                val button = JFXButton(title).apply {
                                                    prefWidth = 200.0
                                                    prefHeight = 50.0
                                                    paddingLeft = 20

                                                    style {
                                                        alignment = Pos.CENTER_LEFT
                                                    }

                                                    isDisableVisualFocus = true

                                                    action { actionHandler?.invoke() }
                                                }
                                                this += button
                                            }

                                            addMenuButton("Cài đặt") { find<SMSettingsView>().openModal() }

                                            addMenuButton("Giới thiệu") { find<SMAboutView>().openModal() }

                                            addMenuButton("Đăng xuất") {
                                                if (serviceCentral.userService.logout()) {
                                                    this@contextualMenu.hide()
                                                    currentStage?.isMaximized = false

                                                    replaceWith<SMLoginFormView>(
                                                        sizeToScene = true,
                                                        centerOnScreen = true
                                                    )
                                                }
                                            }

                                            addMenuButton("Thoát") {
                                                Platform.exit()
                                                System.exit(0)
                                            }
                                        }
                                    }.show(this@menuIcon, PopupVPosition.TOP, PopupHPosition.RIGHT)
                                }
                            }
                        }
                        // endregion
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

                                    backTransition.rate = -1.0

                                    backTransition.play()

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

                    // This is for when the user click outside the drawer
                    setOnDrawerClosing {
                        if (backTransition.rate != -1.0) {
                            backTransition.rate = -1.0
                            backTransition.play()
                        }
                    }
                }

                this += drawer
            }
        }
    }

    /**
     * Method to show a notification under the form of snackbar over the main view
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @param message String
     */
    fun showNotification(message: String) {
        JFXSnackbar(root).enqueue(JFXSnackbar.SnackbarEvent(StackPane().apply {
            paddingAll = 10
            prefWidth = 500.0
            alignment = Pos.CENTER

            label(message) {
                textFill = c("#fff")

                style {
                    fontSize = Dimension(13.0, Dimension.LinearUnits.pt)
                }
            }

            style {
                backgroundColor += c("#000", 0.8)
                backgroundRadius = MultiValue(arrayOf(
                    CssBox(
                        top = Dimension(8.0, Dimension.LinearUnits.px),
                        right = Dimension(8.0, Dimension.LinearUnits.px),
                        bottom = Dimension(8.0, Dimension.LinearUnits.px),
                        left = Dimension(8.0, Dimension.LinearUnits.px)
                    )
                ))
            }
        }, Duration.seconds(3.0), null))
    }
}