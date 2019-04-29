package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassRefreshEvent
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.utils.converters.SMGradeConverter
import com.ebolo.studentmanager.views.utils.ui.tableview.JFXTextFieldTableCell
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.stage.Modality
import javafx.util.StringConverter
import tornadofx.*
import java.time.ZoneOffset

class SMClassStudentPerformanceListFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()
    private val classModel: SMClassModel by param()

    override val root = tableview(classModel.studentList) {
        isEditable = true

        makeIndexColumn("STT").apply {
            style {
                alignment = Pos.TOP_CENTER
            }
        }

        readonlyColumn("Họ và tên lót", SMStudentModel.SMStudentDto::lastName)
        readonlyColumn("Tên", SMStudentModel.SMStudentDto::firstName)

        // Dynamically add the result columns
        for (i in 0 until classModel.numberOfExams.value.toInt()) {
            column<SMStudentModel.SMStudentDto, Int>("Cột điểm ${i + 1}", "grade_$i") {
                isEditable = true

                cellFactory = JFXTextFieldTableCell
                    .forTableColumn<SMStudentModel.SMStudentDto, Int>(SMGradeConverter() as StringConverter<Int>)

                setCellValueFactory { cellData ->
                    val performanceInfo = classModel.studentPerformanceList.value.firstOrNull { info ->
                        info.student == cellData.value.id
                    }

                    SimpleIntegerProperty(
                        if (performanceInfo != null) performanceInfo.results[i]
                        else -1
                    ).observable(
                        getter = SimpleIntegerProperty::get,
                        setter = SimpleIntegerProperty::set,
                        propertyName = "value",
                        propertyType = Int::class
                    )
                }

                this.setOnEditCommit { event ->
                    runAsync {
                        val performanceInfo = (classModel.studentPerformanceList.value.firstOrNull { info ->
                            info.student == event.rowValue.id
                        } ?: SMStudentPerformanceInfo(
                            student = event.rowValue.id,
                            startDate = classModel.startDate.value.atStartOfDay().toInstant(ZoneOffset.UTC),
                            results = generateSequence { -1 }
                                .take(classModel.numberOfExams.value.toInt())
                                .toMutableList()
                        )).apply {
                            this.results[i] = event.newValue
                        }

                        with(serviceCentral.classService) {
                            event.rowValue.updatePerformanceInfo(
                                classId = classModel.id.value,
                                performanceInfo = performanceInfo
                            )
                        }
                    }.ui {
                        smartResize()
                    }
                }

                style {
                    alignment = Pos.TOP_CENTER
                }
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
                classModel.studentPerformanceList.value.setAll(event.classDto.studentPerformanceList)
                classModel.studentList.value.setAll(event.classDto.studentList)

                asyncItems { classModel.studentList.value }.ui {
                    smartResize()
                }
            }

        }
    }
}