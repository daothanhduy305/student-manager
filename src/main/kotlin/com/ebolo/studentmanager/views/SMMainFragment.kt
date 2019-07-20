package com.ebolo.studentmanager.views

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.services.*
import com.ebolo.studentmanager.services.SMGlobal.APP_NAME
import com.ebolo.studentmanager.views.classes.SMClassTableFragment
import com.ebolo.studentmanager.views.settings.SMSettingsFragment
import com.ebolo.studentmanager.views.students.SMStudentTableFragment
import com.ebolo.studentmanager.views.subjects.SMSubjectTableFragment
import com.ebolo.studentmanager.views.teachers.SMTeacherTableFragment
import com.jfoenix.controls.*
import com.jfoenix.controls.JFXPopup.PopupHPosition
import com.jfoenix.controls.JFXPopup.PopupVPosition
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition
import com.sun.javafx.scene.control.GlobalMenuAdapter
import de.codecentric.centerdevice.glass.AdapterContext
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*


class SMMainFragment : Fragment("Student Manager") {
    private val serviceCentral: SMServiceCentral by di()
    private val logger = loggerFor(this::class.java)

    private val subjectTableFragment: SMSubjectTableFragment by lazy { find<SMSubjectTableFragment>() }
    private val studentTableFragment: SMStudentTableFragment by lazy { find<SMStudentTableFragment>() }
    private val classTableFragment: SMClassTableFragment by lazy { find<SMClassTableFragment>() }
    private val teacherTableFragment: SMTeacherTableFragment by lazy { find<SMTeacherTableFragment>() }

    private var drawer: JFXDrawer by singleAssign()
    private var backTransition: HamburgerSlideCloseTransition by singleAssign()
    private var mainViewPanel: BorderPane by singleAssign()

    private val defaultView = "Lớp học"
    private val toolbarTitle = SimpleStringProperty(defaultView)

    private val statusString = SimpleStringProperty("Đang xử lý...")

    override val root = stackpane {
        borderpane {
            top {
                vbox {
                    this += JFXToolbar().apply {
                        paddingAll = 20

                        style {
                            backgroundColor += c("#333")
                        }

                        // region left
                        leftItems += JFXHamburger().apply {
                            backTransition = HamburgerSlideCloseTransition(this).apply {
                                rate = -1.0
                            }

                            onMouseClicked = EventHandler {
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

                                onMouseClicked = EventHandler {
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

                                            addMenuButton("Cài đặt") { find<SMSettingsFragment>().openModal() }

                                            addMenuButton("Giới thiệu") { find<SMAboutView>().openModal() }

                                            addMenuButton("Đăng xuất") {
                                                if (serviceCentral.userService.logout()) {
                                                    this@contextualMenu.hide()
                                                    currentStage?.isMaximized = false

                                                    replaceWith<SMLoginFormFragment>(
                                                        sizeToScene = true,
                                                        centerOnScreen = true
                                                    )
                                                }
                                            }

                                            addMenuButton("Thoát") {
                                                primaryStage.hide()
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
                            "Môn học" -> subjectTableFragment.root
                            "Lớp học" -> classTableFragment.root
                            "Học sinh" -> studentTableFragment.root
                            "Giáo viên" -> teacherTableFragment.root
                            else -> null
                        }

                        bottom {
                            hbox {
                                paddingVertical = 4
                                paddingHorizontal = 8

                                label {
                                    bind(statusString)

                                    StudentManagerApplication.syncCount.onChange {
                                        text = if (it == 0) "" else "Đang xử lý..."
                                    }
                                    subscribe<SMClassListRefreshEvent> {
                                        runLater { StudentManagerApplication.stopSync() }
                                    }
                                    subscribe<SMStudentRefreshEvent> {
                                        runLater { StudentManagerApplication.stopSync() }
                                    }
                                    subscribe<SMTeacherRefreshEvent> {
                                        runLater { StudentManagerApplication.stopSync() }
                                    }
                                    subscribe<SMSubjectRefreshEvent> {
                                        runLater { StudentManagerApplication.stopSync() }
                                    }
                                    subscribe<SMUserListRefreshEvent> {
                                        runLater { StudentManagerApplication.stopSync() }
                                    }
                                }
                            }
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

                        addMenuButton("Môn học", subjectTableFragment.root)
                        addMenuButton("Lớp học", classTableFragment.root)
                        addMenuButton("Học sinh", studentTableFragment.root)
                        addMenuButton("Giáo viên", teacherTableFragment.root)
                    })

                    // This is for when the user click outside the drawer
                    onDrawerClosing = EventHandler {
                        if (backTransition.rate != -1.0) {
                            backTransition.rate = -1.0
                            backTransition.play()
                        }
                    }
                }

                this += drawer
            }
        }

        if (System.getProperty("os.name", "UNKNOWN") == "Mac OS X") {
            // Get the toolkit
            val context = AdapterContext.getContext()

            if (context != null) {
                val systemMenuAdapter = context.systemMenuAdapter
                val applicationAdapter = context.applicationAdapter

                try {
                    systemMenuAdapter.setAppleMenu(GlobalMenuAdapter.adapt(Menu(
                        APP_NAME,
                        null
                    ).apply {
                        item("Giới thiệu") {
                            action { tornadofx.find<SMAboutView>().openModal() }
                        }

                        item("Cài đặt...") {
                            action { find<SMSettingsFragment>().openModal() }
                            accelerator = KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN)
                        }

                        separator()

                        item("Ẩn $APP_NAME") {
                            action { applicationAdapter.hide() }
                            accelerator = KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN)
                        }

                        item("Ẩn các app(s) khác") {
                            action { applicationAdapter.hideOtherApplications() }
                            accelerator = KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN, KeyCombination.ALT_DOWN)
                        }

                        item("Hiện lại các app(s) khác") {
                            action { applicationAdapter.unhideAllApplications() }
                        }

                        separator()

                        item("Đăng xuất") {
                            action {
                                if (serviceCentral.userService.logout()) {
                                    currentStage?.isMaximized = false

                                    replaceWith<SMLoginFormFragment>(
                                        sizeToScene = true,
                                        centerOnScreen = true
                                    )
                                }
                            }
                        }

                        item("Thoát") {
                            action { applicationAdapter.quit() }
                            accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN)
                        }
                    }))
                } catch (e: Throwable) {
                    logger.warn("Cannot construct the menu for this Mac version")
                }
            }

            /*menubar {
                useSystemMenuBarProperty().set(true)
            }*/
        }

        subscribe<SMRestartAppRequest> { event ->
            when (event.restartMode) {
                RestartMode.FULL -> replaceWith<SMInitFragment>(sizeToScene = true, centerOnScreen = true)
                RestartMode.PARTIAL -> replaceWith<SMSplashFragment>(sizeToScene = true, centerOnScreen = true)
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