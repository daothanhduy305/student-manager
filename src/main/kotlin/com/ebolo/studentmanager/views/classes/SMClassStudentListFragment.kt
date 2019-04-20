package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassRefreshEvent
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.utils.SMViewUtils
import javafx.geometry.Pos
import javafx.stage.Modality
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import tornadofx.*

class SMClassStudentListFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()
    private val classModel: SMClassModel by param()

    override val root = tableview(classModel.studentList.value) {
        isEditable = true

        makeIndexColumn("STT").apply {
            style {
                alignment = Pos.TOP_CENTER
            }
        }

        readonlyColumn("Họ và tên lót", SMStudentModel.SMStudentDto::lastName)
        readonlyColumn("Tên", SMStudentModel.SMStudentDto::firstName)

        // Dynamically add the grade columns
        for (i in 0..(classModel.numberOfExams.value.toInt() - 1)) {
            isEditable = true

            column<SMStudentModel.SMStudentDto, Int>("Cột điểm ${i + 1}", "grade_$i") {
                cellFactory = SMViewUtils.JFXTextFieldTableCell.forTableColumn<SMStudentModel.SMStudentDto, Int>(IntegerStringConverter() as StringConverter<Int>)
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
                classModel.item = event.classDto

                asyncItems { classModel.studentList.value }
            }

        }
    }
}