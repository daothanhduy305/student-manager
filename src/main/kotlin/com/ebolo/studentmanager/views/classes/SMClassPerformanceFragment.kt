package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.models.SMStudentPerformanceModel
import com.ebolo.studentmanager.services.SMGlobal
import com.ebolo.studentmanager.services.SMServiceCentral
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDatePicker
import com.jfoenix.controls.JFXTextArea
import com.jfoenix.controls.JFXTextField
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*

class SMClassPerformanceFragment : Fragment(SMGlobal.APP_NAME) {
    private val serviceCentral: SMServiceCentral by di()

    private val studentInfo: SMStudentModel.SMStudentDto by param()
    private val performanceInfo: SMStudentPerformanceInfo by param()
    private val classId: String by param()

    private val performanceInfoModel by lazy {
        SMStudentPerformanceModel(SMStudentPerformanceModel.SMStudentPerformanceDto(performanceInfo))
    }

    override val root = vbox {
        paddingAll = 20

        label("Thông tin cho học viên ${studentInfo.lastName} ${studentInfo.firstName}") {
            paddingLeft = 10
            paddingBottom = 30

            style {
                fontSize = Dimension(18.0, Dimension.LinearUnits.pt)
            }
        }

        form {
            fieldset(labelPosition = Orientation.VERTICAL) {
                hbox {
                    spacing = 30.0

                    vbox {
                        spacing = 20.0

                        for (i in 0 until performanceInfo.results.size) {
                            field("Cột điểm ${i + 1}") {
                                this += JFXTextField().apply {
                                    bind(performanceInfoModel.item.resultsPropertyList[i])
                                }
                            }
                        }
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
            alignment = Pos.BOTTOM_RIGHT
            spacing = 20.0
            paddingVertical = 20

            this += JFXButton("Hủy bỏ").apply {
                buttonType = JFXButton.ButtonType.RAISED
                paddingVertical = 15
                paddingHorizontal = 30

                action { modalStage?.close() }

                style {
                    backgroundColor += c("#ff5533")
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
                            studentInfo.updatePerformanceInfo(classId, performanceInfoModel.toEntity())
                        }
                    }.ui { modalStage?.close() }
                }
            }
        }
    }
}