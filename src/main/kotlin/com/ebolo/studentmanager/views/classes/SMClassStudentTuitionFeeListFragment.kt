package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassRefreshEvent
import com.ebolo.studentmanager.services.SMFeePaidRefreshRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.utils.ui.tableview.JFXCheckboxTableCell
import com.ebolo.studentmanager.views.utils.ui.tableview.JFXDatePickerTableCell
import com.ebolo.studentmanager.views.utils.ui.tableview.eboloObservable
import com.jfoenix.controls.JFXDatePicker
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.AccessibleAttribute
import javafx.scene.control.TableCell
import tornadofx.*
import java.time.LocalDate
import java.time.ZoneOffset

class SMClassStudentTuitionFeeListFragment : Fragment() {
    private val paymentDateColumnId = "paymentDateColumn"

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
                }.weightedWidth(1, 30.0, true)

                readonlyColumn("Họ và tên lót", SMStudentModel.SMStudentDto::lastName).weightedWidth(5, 20.0, true)

                readonlyColumn("Tên", SMStudentModel.SMStudentDto::firstName).weightedWidth(5, 20.0, true)

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
                            } ui {
                                val columnIndex = tableView.columns.indexOfFirst { it.id == paymentDateColumnId }
                                val rowIndex = classModel.studentList.value.indexOfFirst { it.id == studentDto.id }

                                @Suppress("UNCHECKED_CAST")
                                val cell = tableView!!.queryAccessibleAttribute(
                                    AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                    rowIndex,
                                    columnIndex
                                ) as TableCell<SMStudentModel.SMStudentDto, LocalDate?>

                                cell.isDisable = !value
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
                }.weightedWidth(2, 20.0, true)

                column<SMStudentModel.SMStudentDto, LocalDate?>("Ngày đóng học phí", "paymentDate") {
                    id = paymentDateColumnId

                    cellFactory = JFXDatePickerTableCell.forTableColumn(
                        onValueChanged = { studentDto, value ->
                            if (studentDto != null) {
                                runAsync {
                                    with(serviceCentral.classService) {
                                        classModel.item.updateFeePaidDate(studentDto.id, choosingDate.value, value)
                                    }
                                }
                            }
                        },
                        setup = {
                            it.defaultColor = c("#3f51b5")
                            it.isOverLay = false
                        },
                        enablePredicate = { studentDto ->
                            logger.info("Called to this")
                            if (studentDto != null) {
                                paymentInfoList.firstOrNull { info ->
                                    info.studentId == studentDto.id
                                } == null
                            } else {
                                false
                            }
                        }
                    )

                    setCellValueFactory { cellData ->
                        val paymentInfo = paymentInfoList.firstOrNull { info ->
                            info.studentId == cellData.value.id
                        }

                        SimpleObjectProperty(paymentInfo?.paidDate?.atOffset(ZoneOffset.UTC)?.toLocalDate())
                            .eboloObservable(
                                getter = SimpleObjectProperty<LocalDate?>::get,
                                setter = SimpleObjectProperty<LocalDate?>::set,
                                propertyName = "value"
                            )
                    }

                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }.weightedWidth(5, 20.0, true)

                column<SMStudentModel.SMStudentDto, Int>("Số tháng đã đóng", "totalPaymentMade") {
                    setCellValueFactory { cellData ->
                        val totalMade = paymentInfoList.count { info ->
                            info.studentId == cellData.value.id
                        }

                        SimpleIntegerProperty(totalMade)
                            .observable(
                                getter = SimpleIntegerProperty::get,
                                setter = SimpleIntegerProperty::set,
                                propertyName = "value"
                            )
                    }

                    style {
                        alignment = Pos.TOP_CENTER
                    }
                }.weightedWidth(5, 20.0, true)

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

                subscribe<SMFeePaidRefreshRequest> { request ->
                    val classId = request.classId

                    if (classId == classModel.id.value) {
                        runAsync {
                            with(serviceCentral.classService) {
                                val date = LocalDate.now()
                                val classDto = classModel.item
                                classDto.getTuitionFeePaymentInfo(date)
                            }
                        } ui {
                            paymentInfoList.setAll(it)
                            refresh()
                        }
                    }
                }

                choosingDate.addListener { _, oldValue, newValue ->
                    runAsync {
                        logger.info("Changing the date from $oldValue to $newValue. Refreshing data...")

                        paymentInfoList.setAll(
                            with(serviceCentral.classService) {
                                val classDto = classModel.item
                                classDto.getTuitionFeePaymentInfo(newValue)
                            }
                        )
                    }.ui { refresh() }
                }
            }
        }
    }
}