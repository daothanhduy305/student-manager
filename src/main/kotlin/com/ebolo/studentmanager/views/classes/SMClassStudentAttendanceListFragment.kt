package com.ebolo.studentmanager.views.classes

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
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
import javafx.stage.Modality
import tornadofx.*
import java.time.LocalDate

class SMClassStudentAttendanceListFragment : Fragment() {
    val logger = loggerFor(SMClassStudentAttendanceListFragment::class.java)
    private val serviceCentral: SMServiceCentral by di()
    private val classModel: SMClassModel by param()
    private val choosingDate = SimpleObjectProperty<LocalDate>(LocalDate.now())

    private val attendanceInfoList by lazy {
        with(serviceCentral.classService) {
            val date = LocalDate.now()
            val classDto = classModel.item
            classDto.getAttendanceInfoList(date)
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

                column<SMStudentModel.SMStudentDto, Boolean>("Điểm danh", "attendance") {
                    cellFactory = JFXCheckboxTableCell.forTableColumn { studentDto, value ->
                        if (studentDto != null) {
                            runAsync {
                                with(serviceCentral.classService) {
                                    if (value) {
                                        classModel.item.deleteAbsenceInfo(studentDto.id, choosingDate.value)
                                    } else {
                                        classModel.item.addAbsenceInfo(studentDto.id, choosingDate.value)
                                    }
                                }
                            }
                        }
                    }

                    setCellValueFactory { cellData ->
                        val attendanceInfo = attendanceInfoList.firstOrNull { info ->
                            info.studentId == cellData.value.id
                        }

                        SimpleBooleanProperty(attendanceInfo == null)
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

                // set up the context menu
                contextmenu {
                    item("Sửa...").action {
                        if (selectedItem != null) {
                            val performanceInfo = classModel.item.studentPerformanceList.firstOrNull { performanceInfo ->
                                performanceInfo.student == selectedItem!!.id
                            } ?: SMStudentPerformanceInfo(
                                student = selectedItem!!.id,
                                results = generateSequence { -1 }.take(classModel.numberOfExams.value.toInt()).toMutableList())

                            find<SMClassPerformanceFragment>(
                                "studentInfo" to selectedItem,
                                "performanceInfo" to performanceInfo,
                                "classId" to classModel.id.value)
                                .openModal(modality = Modality.WINDOW_MODAL, block = true)
                        }
                    }

                    item("Xóa").action {
                        if (selectedItem != null) runAsync {
                            with(serviceCentral.classService) {
                                selectedItem!! deregisterFromClass classModel
                            }
                        }
                    }
                }

                // Subscribe to events
                subscribe<SMClassRefreshEvent> { event ->
                    if (event.classDto.id == classModel.item.id) {
                        logger.info("Receiving refresh event for class ${event.classDto.id}")

                        classModel.studentPerformanceList.value.setAll(event.classDto.studentPerformanceList)
                        classModel.studentList.value.setAll(event.classDto.studentList)

                        asyncItems { classModel.studentList.value }.ui {
                            smartResize()
                        }
                    }

                }

                choosingDate.addListener { _, oldValue, newValue ->
                    runAsync {
                        logger.info("Changing the date from $oldValue to $newValue. Refreshing data...")

                        attendanceInfoList.setAll(
                            with(serviceCentral.classService) {
                                val classDto = classModel.item
                                classDto.getAttendanceInfoList(newValue)
                            }.observable()
                        )
                    }.ui { refresh() }
                }
            }
        }
    }
}