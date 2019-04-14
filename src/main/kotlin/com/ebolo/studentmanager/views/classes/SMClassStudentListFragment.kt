package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMStudentRefreshRequest
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.ebolo.studentmanager.views.students.SMStudentInfoFragment
import javafx.stage.Modality
import tornadofx.*

class SMClassStudentListFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()
    private val classModel: SMClassModel by param()

    override val root = tableview(classModel.studentList.value) {
        readonlyColumn("Tên", SMStudentModel.SMStudentDto::firstName)
        readonlyColumn("Họ", SMStudentModel.SMStudentDto::lastName)
        readonlyColumn("Nickname", SMStudentModel.SMStudentDto::nickname)
        readonlyColumn("Sinh nhật", SMStudentModel.SMStudentDto::birthday)
        readonlyColumn("Học vấn", SMStudentModel.SMStudentDto::educationLevel) {
            cellFormat { text = it.title }
        }
        readonlyColumn("Số điện thoại", SMStudentModel.SMStudentDto::phone)

        smartResize()

        // set up the context menu
        contextmenu {
            item("Sửa...").action {
                find<SMStudentInfoFragment>(
                    "mode" to SMCRUDUtils.CRUDMode.EDIT,
                    "studentModel" to SMStudentModel(selectedItem))
                    .openModal(modality = Modality.WINDOW_MODAL, block = true)
            }

            item("Xóa").action {
                if (selectedItem != null) runAsync {
                    serviceCentral.studentService.deleteStudent(selectedItem!!.id)
                    fire(SMStudentRefreshRequest)
                }
            }
        }
    }
}