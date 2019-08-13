package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.models.SMStudentPerformanceModel
import com.ebolo.studentmanager.services.SMFeePaidRefreshRequest
import com.ebolo.studentmanager.services.SMGlobal
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMTheme
import com.ebolo.studentmanager.views.utils.ui.tableview.JFXCheckboxTableCell
import com.jfoenix.controls.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import tornadofx.*
import java.time.LocalDate

class SMClassPerformanceFragment : Fragment(SMGlobal.APP_NAME) {
    private val serviceCentral: SMServiceCentral by di()

    private val studentInfo: SMStudentModel.SMStudentDto by param()
    private val performanceInfo: SMStudentPerformanceInfo by param()
    private val classInfo: SMClassModel.SMClassDto by param()

    private val performanceInfoModel by lazy {
        val info = SMStudentPerformanceModel.SMStudentPerformanceDto(performanceInfo)

        if (classInfo.numberOfExams.toInt() > info.resultsPropertyList.size) {
            for (i in info.resultsPropertyList.size until classInfo.numberOfExams.toInt()) {
                info.resultsPropertyList.add(SimpleStringProperty(""))
            }
        }

        SMStudentPerformanceModel(info)
    }

    private val tuitionFeeStatus by lazy {
        with(serviceCentral.classService) {
            classInfo.getTuitionFeePaymentInfo(studentInfo.id)
        }.asObservable()
    }

    override val root = stackpane {
        style {
            backgroundColor += c("#fff")
        }

        this += JFXTabPane().apply {
            tab("Học lực") {
                vbox {
                    paddingAll = 20

                    label("Thông tin cho học viên ${studentInfo.lastName} ${studentInfo.firstName}") {
                        paddingLeft = 10
                        paddingBottom = 15

                        style {
                            fontSize = Dimension(18.0, Dimension.LinearUnits.pt)
                        }
                    }

                    label("Lớp: ${classInfo.name}") {
                        paddingLeft = 10
                        paddingBottom = 30

                        style {
                            fontSize = Dimension(16.0, Dimension.LinearUnits.pt)
                        }
                    }

                    form {
                        fieldset(labelPosition = Orientation.VERTICAL) {
                            hbox {
                                this += JFXScrollPane().apply {
                                    maxHeight = 500.0

                                    stackpane {
                                        this += JFXListView<VBox>().apply {
                                            styleClass.add("mylistview")

                                            for (i in 0 until classInfo.numberOfExams.toInt()) {
                                                items.add(VBox().apply {
                                                    children.addAll(
                                                        Label("Cột điểm ${i + 1}").apply {
                                                            style {
                                                                textFill = c("#000")
                                                            }
                                                        },
                                                        JFXTextField().apply {
                                                            bind(performanceInfoModel.item.resultsPropertyList[i])
                                                        }
                                                    )
                                                })
                                            }
                                        }
                                    }

                                    JFXScrollPane.smoothScrolling(this.children[0] as ScrollPane)
                                }

                                pane {
                                    minWidth = 1.0
                                    prefWidth = 1.0
                                    maxWidth = 1.0

                                    fitToParentHeight()

                                    style {
                                        backgroundColor += c("#ccc")
                                    }
                                }

                                vbox {
                                    paddingLeft = 30
                                    spacing = 20.0

                                    field("Ngày bắt đầu") {
                                        this += JFXDatePicker().apply {
                                            bind(performanceInfoModel.startDate)

                                            defaultColor = c("#3f51b5")
                                            isOverLay = false
                                        }
                                    }

                                    field("Nhận xét") {
                                        this += JFXTextArea().apply {
                                            bind(performanceInfoModel.note)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    hbox(spacing = 20) {
                        alignment = Pos.TOP_RIGHT
                        spacing = 20.0
                        paddingVertical = 20

                        this += JFXButton("Hủy bỏ").apply {
                            buttonType = JFXButton.ButtonType.RAISED
                            paddingVertical = 15
                            paddingHorizontal = 30

                            action { modalStage?.close() }

                            style {
                                backgroundColor += c(SMTheme.CANCEL_BUTTON_COLOR)
                                textFill = c("#fff")
                            }
                        }

                        this += JFXButton("Lưu lại").apply {
                            buttonType = JFXButton.ButtonType.RAISED
                            paddingVertical = 15
                            paddingHorizontal = 30

                            style {
                                backgroundColor += c("#ffffff")
                            }

                            action {
                                runAsync {
                                    with(serviceCentral.classService) {
                                        studentInfo.updatePerformanceInfo(classInfo.id, performanceInfoModel.toEntity())
                                    }
                                }.ui { modalStage?.close() }
                            }
                        }
                    }
                }
            }

            tab("Học phí") {
                tableview(tuitionFeeStatus) {
                    makeIndexColumn("STT").apply {
                        style {
                            alignment = Pos.TOP_CENTER
                        }
                    }.weightedWidth(1, 30.0, true)

                    column<Pair<LocalDate, Boolean>, Boolean>("Đã đóng", "payment") {
                        cellFactory = JFXCheckboxTableCell.forTableColumn { info, value ->
                            if (info != null) {
                                runAsync {
                                    with(serviceCentral.classService) {
                                        if (value) {
                                            classInfo.addFeePaidInfo(studentInfo.id, info.first)
                                        } else {
                                            classInfo.deleteFeePaidInfo(studentInfo.id, info.first)
                                        }
                                    }
                                    fire(SMFeePaidRefreshRequest(classInfo.id, studentInfo.id))
                                }
                            }
                        }

                        setCellValueFactory { cellData ->
                            SimpleBooleanProperty(cellData.value.second)
                                .observable(
                                    getter = SimpleBooleanProperty::get,
                                    setter = SimpleBooleanProperty::set,
                                    propertyName = "value",
                                    propertyType = Boolean::class
                                )
                        }

                        style {
                            alignment = Pos.TOP_CENTER
                        }
                    }.weightedWidth(1, 20.0, true)

                    column<Pair<LocalDate, Boolean>, String>("", "time") {
                        setCellValueFactory { cellData ->
                            val month = cellData.value.first.month.value
                            val year = cellData.value.first.year
                            SimpleStringProperty("Tháng $month, năm $year")
                                .observable(
                                    getter = SimpleStringProperty::get,
                                    setter = SimpleStringProperty::set,
                                    propertyName = "value",
                                    propertyType = String::class
                                )
                        }

                        style {
                            alignment = Pos.TOP_LEFT
                        }
                    }.weightedWidth(8, 20.0, true)

                    smartResize()
                }
            }
        }
    }
}