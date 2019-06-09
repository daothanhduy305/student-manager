package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassListForStudentRefreshEvent
import com.ebolo.studentmanager.services.SMClassListForStudentRefreshRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.classes.SMClassPerformanceFragment
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.time.LocalDate
import java.time.ZoneOffset

class SMRegisteredClassesFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()

    private val studentModel: SMStudentModel by param(SMStudentModel())
    private val classesList by lazy {
        serviceCentral.classService.getClassListOfStudent(studentModel.id.value).observable()
    }

    override val root = borderpane {
        center {
            tableview(classesList) {
                readonlyColumn("Tên lớp", SMClassModel.SMClassDto::name)

                readonlyColumn("Giáo viên", SMClassModel.SMClassDto::teacher) {
                    cellFormat { teacher -> text = "${teacher.lastName} ${teacher.firstName}" }
                }

                readonlyColumn("Môn", SMClassModel.SMClassDto::subject) {
                    cellFormat { subject -> text = subject.name }
                }

                column<SMClassModel.SMClassDto, LocalDate>("Ngày bắt đầu", "studentStartDate") {
                    setCellValueFactory { cellData ->
                        val performanceInfo = cellData.value.studentPerformanceList.firstOrNull { it.student == studentModel.id.value }

                        if (performanceInfo != null)
                            SimpleObjectProperty(performanceInfo.startDate?.atOffset(ZoneOffset.UTC)?.toLocalDate())
                        else
                            SimpleObjectProperty<LocalDate>()
                    }
                }

                contextmenu {
                    item("Sửa...").action {
                        if (selectedItem != null) {
                            val performanceInfo = selectedItem!!.studentPerformanceList.firstOrNull { performanceInfo ->
                                performanceInfo.student == studentModel.id.value
                            } ?: SMStudentPerformanceInfo(
                                student = studentModel.id.value,
                                results = generateSequence { -1 }.take(selectedItem!!.numberOfExams.toInt()).toMutableList())

                            find<SMClassPerformanceFragment>(
                                "studentInfo" to studentModel.item,
                                "performanceInfo" to performanceInfo,
                                "classId" to selectedItem!!.id)
                                .openModal()
                        }
                    }

                    item("Xóa").action {
                        if (selectedItem != null) runAsync {
                            with(serviceCentral.classService) {
                                val result = studentModel.item deregisterFromClass SMClassModel(selectedItem!!)
                                if (result.success)
                                    fire(SMClassListForStudentRefreshRequest(studentModel.id.value))
                            }
                        }
                    }
                }

                setOnMouseClicked {
                    if (it.clickCount == 2) {
                        if (selectedItem != null) {
                            val performanceInfo = selectedItem!!.studentPerformanceList.firstOrNull { performanceInfo ->
                                performanceInfo.student == studentModel.id.value
                            } ?: SMStudentPerformanceInfo(
                                student = studentModel.id.value,
                                results = generateSequence { -1 }.take(selectedItem!!.numberOfExams.toInt()).toMutableList())

                            find<SMClassPerformanceFragment>(
                                "studentInfo" to studentModel.item,
                                "performanceInfo" to performanceInfo,
                                "classId" to selectedItem!!.id)
                                .openModal()
                        }
                    }
                }

                smartResize()

                // subscribe to events
                subscribe<SMClassListForStudentRefreshEvent> { event ->
                    asyncItems { event.classes } ui { requestResize() }
                }
            }
        }
    }
}
