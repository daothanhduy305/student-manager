package com.ebolo.studentmanager.views.classes

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMAttendanceListRefreshRequest
import com.ebolo.studentmanager.services.SMClassRefreshEvent
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.utils.ui.tableview.JFXCheckboxTableCell
import com.jfoenix.controls.JFXDatePicker
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*
import java.time.LocalDate

class SMClassStudentAttendanceListFragment : Fragment() {
    val logger = loggerFor(SMClassStudentAttendanceListFragment::class.java)
    private val serviceCentral: SMServiceCentral by di()
    private val classModel: SMClassModel by param()
    private val choosingDate = SimpleObjectProperty<LocalDate>(LocalDate.now())

    private val attendanceInfoList by lazy {
        with(serviceCentral.classService) {
            // val date = LocalDate.now()
            val classDto = classModel.item
            classDto.getAttendanceInfoList()
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
                                    val result = if (value) {
                                        classModel.item.deleteAbsenceInfo(studentDto.id, choosingDate.value)
                                    } else {
                                        classModel.item.addAbsenceInfo(studentDto.id, choosingDate.value)
                                    }

                                    if (result.success) fire(SMAttendanceListRefreshRequest(classModel.id.value))
                                }
                            }
                        }
                    }

                    setCellValueFactory { cellData ->
                        val attendanceInfo = attendanceInfoList.firstOrNull { info ->
                            val date = choosingDate.get()
                            info.studentId == cellData.value.id
                                && info.day == date.dayOfMonth
                                && info.month == date.month
                                && info.year == date.year
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

                column<SMStudentModel.SMStudentDto, Int>("Số ngày nghỉ", "absence") {
                    setCellValueFactory { cellData ->
                        val absenceCount = attendanceInfoList.count { it.studentId == cellData.value.id }

                        SimpleIntegerProperty(absenceCount)
                            .observable(
                                getter = SimpleIntegerProperty::get,
                                setter = SimpleIntegerProperty::set,
                                propertyName = "value",
                                propertyType = Int::class
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

                        asyncItems { classModel.studentList.value }.ui {
                            refresh()
                        }
                    }

                }

                choosingDate.addListener { _, oldValue, newValue ->
                    logger.info("Changing the date from $oldValue to $newValue. Refreshing data...")

                    fire(SMAttendanceListRefreshRequest(classModel.id.value))
                }

                subscribe<SMAttendanceListRefreshRequest> { request ->
                    if (request.classId == classModel.id.value) refreshAttendanceList().ui { refresh() }
                }
            }
        }
    }

    /**
     * Private method to be called to refresh the attendance list
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @return Task<Boolean>
     */
    private fun refreshAttendanceList() = runAsync {
        attendanceInfoList.setAll(
            with(serviceCentral.classService) {
                val classDto = classModel.item
                classDto.getAttendanceInfoList()
            }.observable()
        )
    }
}