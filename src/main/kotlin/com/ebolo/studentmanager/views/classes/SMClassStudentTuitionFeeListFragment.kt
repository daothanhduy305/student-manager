package com.ebolo.studentmanager.views.classes

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassRefreshEvent
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.utils.ui.tableview.JFXCheckboxTableCell
import com.jfoenix.controls.JFXDatePicker
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*
import java.time.LocalDate

class SMClassStudentTuitionFeeListFragment : Fragment() {
    private val logger = loggerFor(SMClassStudentTuitionFeeListFragment::class.java)
    private val serviceCentral: SMServiceCentral by di()
    private val classModel: SMClassModel by param()
    private val choosingDate = SimpleObjectProperty<LocalDate>(LocalDate.now())

    private val paymentInfoList by lazy {
        with(serviceCentral.classService) {
            val date = LocalDate.now()
            val classDto = classModel.item
            classDto.getTuitionFeePaymentInfo(date)
        }.observable()
    }

    override val root = borderpane {
        top {
            form {
                fieldset(labelPosition = Orientation.HORIZONTAL) {
                    field("Chọn ngày") {
                        this += JFXDatePicker().apply {
                            bind(choosingDate)

                            defaultColor = c("#3f51b5")
                            isOverLay = false
                        }
                    }
                }
            }
        }

        center {
            tableview(classModel.studentList) {
                isEditable = true

                makeIndexColumn("STT").apply {
                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }

                readonlyColumn("Họ và tên lót", SMStudentModel.SMStudentDto::lastName)
                readonlyColumn("Tên", SMStudentModel.SMStudentDto::firstName)

                column<SMStudentModel.SMStudentDto, Boolean>("Học phí", "payment") {
                    cellFactory = JFXCheckboxTableCell.forTableColumn { studentDto, value ->
                        if (studentDto != null) {
                            runAsync {
                                with(serviceCentral.classService) {
                                    if (value) {
                                        classModel.item.addFeePaidInfo(studentDto.id, choosingDate.value)
                                    } else {
                                        classModel.item.deleteFeePaidInfo(studentDto.id, choosingDate.value)
                                    }
                                }
                            }
                        }
                    }

                    setCellValueFactory { cellData ->
                        val paymentInfo = paymentInfoList.firstOrNull { info ->
                            info.studentId == cellData.value.id
                        }

                        SimpleBooleanProperty(paymentInfo != null)
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
                }

                smartResize()

                // Subscribe to events
                subscribe<SMClassRefreshEvent> { event ->
                    if (event.classDto.id == classModel.item.id) {
                        logger.info("Receiving refresh event for class ${event.classDto.id}")

                        classModel.studentPerformanceList.value.setAll(event.classDto.studentPerformanceList)
                        classModel.studentList.value.setAll(event.classDto.studentList)

                        asyncItems { classModel.studentList.value }.ui { requestResize() }
                    }

                }

                choosingDate.addListener { _, oldValue, newValue ->
                    runAsync {
                        logger.info("Changing the date from $oldValue to $newValue. Refreshing data...")

                        paymentInfoList.setAll(
                            with(serviceCentral.classService) {
                                val classDto = classModel.item
                                classDto.getTuitionFeePaymentInfo(newValue)
                            }.observable()
                        )
                    }.ui { refresh() }
                }
            }
        }
    }
}