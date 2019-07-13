package com.ebolo.studentmanager.views.classes

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassListRefreshRequest
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMSubjectRefreshEvent
import com.ebolo.studentmanager.services.SMTeacherRefreshEvent
import com.ebolo.studentmanager.utils.SMCRUDUtils
import com.ebolo.studentmanager.utils.formatDecimal
import com.ebolo.studentmanager.utils.isFormattedLong
import com.ebolo.studentmanager.views.subjects.SMSubjectInfoFragment
import com.ebolo.studentmanager.views.teachers.SMTeacherInfoFragment
import com.jfoenix.controls.*
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleLongProperty
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.TabPane
import javafx.scene.control.TextFormatter
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import org.apache.commons.lang3.StringUtils
import tornadofx.*


class SMClassInfoFragment : Fragment("Thông tin lớp học") {
    private val serviceCentral: SMServiceCentral by di()
    private val mode: SMCRUDUtils.CRUDMode by param()
    private val classModel: SMClassModel by param(SMClassModel())

    private val subjectList by lazy { serviceCentral.subjectService.getSubjects().observable() }
    private val teacherList by lazy { serviceCentral.teacherService.getTeacherList().observable() }
    private val studentList by lazy { serviceCentral.studentService.getStudentList().observable() }

    private val month = SimpleLongProperty()
    private val fee = SimpleLongProperty()

    private val isProcessing = SimpleBooleanProperty(false)

    override val root = stackpane {
        title = when (mode) {
            SMCRUDUtils.CRUDMode.NEW -> "Thêm lớp mới"
            else -> "Thông tin lớp ${classModel.name.value}"
        }

        style {
            backgroundColor += c("#fff")
        }

        this += JFXTabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab("Thông tin lớp") {
                form {
                    paddingAll = 20

                    vbox {
                        vgrow = Priority.ALWAYS

                        hbox {
                            vgrow = Priority.ALWAYS
                            spacing = 40.0

                            fieldset(labelPosition = Orientation.VERTICAL) {
                                spacing = 20.0
                                prefWidth = 300.0

                                field("Tên lớp *") {
                                    this += JFXTextField().apply {
                                        bind(classModel.name)
                                        required()
                                    }
                                }

                                field("Môn học *") {
                                    hbox(spacing = 10) {
                                        this += JFXComboBox(subjectList).apply {
                                            bind(classModel.subject)
                                            hgrow = Priority.ALWAYS

                                            value = if (mode == SMCRUDUtils.CRUDMode.NEW) {
                                                subjectList.firstOrNull()
                                            } else {
                                                subjectList.firstOrNull { it.id == classModel.item.subject.id }
                                            }

                                            vgrow = Priority.ALWAYS
                                            useMaxWidth = true

                                            required()

                                            subscribe<SMSubjectRefreshEvent> { event ->
                                                asyncItems { event.subjects }
                                            }
                                        }

                                        hbox {
                                            alignment = Pos.BOTTOM_LEFT

                                            this += MaterialIconView(MaterialIcon.ADD).apply {
                                                fill = c("#3f51b5")
                                                glyphSize = 24

                                                setOnMouseClicked {
                                                    find<SMSubjectInfoFragment>().openModal()
                                                }
                                            }
                                        }
                                    }
                                }

                                field("Giáo viên *") {
                                    hbox(spacing = 10) {
                                        this += JFXComboBox(teacherList).apply {
                                            hgrow = Priority.ALWAYS

                                            bind(classModel.teacher)

                                            value = if (mode == SMCRUDUtils.CRUDMode.NEW) {
                                                teacherList.firstOrNull()
                                            } else {
                                                teacherList.firstOrNull { it.id == classModel.item.teacher.id }
                                            }

                                            vgrow = Priority.ALWAYS
                                            useMaxWidth = true

                                            required()

                                            subscribe<SMTeacherRefreshEvent> { event ->
                                                teacherList.setAll(event.teachers)
                                            }
                                        }

                                        hbox {
                                            alignment = Pos.BOTTOM_LEFT

                                            this += MaterialIconView(MaterialIcon.ADD).apply {
                                                fill = c("#3f51b5")
                                                glyphSize = 24

                                                setOnMouseClicked {
                                                    find<SMTeacherInfoFragment>().openModal()
                                                }
                                            }
                                        }
                                    }
                                }

                                field("Học phí *") {
                                    this += JFXTextField().apply {
                                        textFormatter = TextFormatter(object : StringConverter<Number?>() {
                                            override fun fromString(string: String?): Number? {
                                                return if (string != null && string.isFormattedLong())
                                                    string.trim().replace("[^\\d]".toRegex(), "").toLong()
                                                else null
                                            }

                                            override fun toString(number: Number?): String {
                                                return if (number != null) return number.toLong().toString().formatDecimal()
                                                else ""
                                            }
                                        })

                                        bind(fee)
                                        bind(classModel.tuitionFee)

                                        textProperty().onChange {
                                            runLater { commitValue() }
                                        }

                                        validator { text ->
                                            when {
                                                text.isNullOrBlank() -> error("This field is required")
                                                !text.isFormattedLong() -> error("Number is required")
                                                else -> null
                                            }
                                        }

                                        required()
                                    }
                                }

                                field("Không tính học phí tự động") {
                                    this += JFXCheckBox().apply {
                                        bind(classModel.customTotalFee)

                                        classModel.customTotalFee.onChange { classModel.validate() }
                                    }
                                }

                                field("Học phí toàn khóa (dự kiến)") {
                                    disableWhen(classModel.customTotalFee)

                                    hbox(spacing = 5) {
                                        paddingTop = 5
                                        val courseFee = Bindings.multiply(month, fee)

                                        label {
                                            text = if (courseFee.value == 0L) "Miễn phí" else courseFee
                                                .value.toString().formatDecimal()

                                            courseFee.onChange { newNumber ->
                                                if (newNumber != null) text =
                                                    if (newNumber == 0L) "Miễn phí"
                                                    else newNumber.toString().formatDecimal()
                                            }

                                            style {
                                                fontSize = Dimension(14.0, Dimension.LinearUnits.pt)
                                            }
                                        }

                                        label("(VND)") {
                                            style {
                                                fontSize = Dimension(14.0, Dimension.LinearUnits.pt)
                                            }

                                            visibleWhen(Bindings.greaterThan(courseFee, 0))
                                        }
                                    }
                                }

                                field("Học phí toàn khóa") {
                                    enableWhen(classModel.customTotalFee)

                                    hbox(spacing = 10) {
                                        alignment = Pos.BOTTOM_LEFT

                                        this += JFXTextField().apply {
                                            hgrow = Priority.ALWAYS

                                            textFormatter = TextFormatter(object : StringConverter<Number?>() {
                                                override fun fromString(string: String?): Number? {
                                                    return if (string != null && string.isFormattedLong())
                                                        string.trim().replace("[^\\d]".toRegex(), "").toLong()
                                                    else null
                                                }

                                                override fun toString(number: Number?): String {
                                                    return if (number != null) return number.toLong().toString().formatDecimal()
                                                    else ""
                                                }
                                            })

                                            bind(classModel.totalTuitionFee)

                                            textProperty().onChange {
                                                runLater { commitValue() }
                                            }

                                            validator { text ->
                                                when {
                                                    !isDisabled && text.isNullOrBlank() -> error("This field is required")
                                                    !isDisabled && !text.isNullOrBlank() && !text.isFormattedLong() -> error("Number is required")
                                                    else -> null
                                                }
                                            }
                                        }

                                        label("(VND)") {
                                            style {
                                                fontSize = Dimension(12.0, Dimension.LinearUnits.pt)
                                            }
                                        }
                                    }
                                }
                            }

                            fieldset(labelPosition = Orientation.VERTICAL) {
                                spacing = 20.0

                                field("Số cột điểm *") {
                                    this += JFXTextField().apply {
                                        bind(classModel.numberOfExams)
                                        required()
                                        validator { text ->
                                            if (text != null)
                                                if (text.isInt()) {
                                                    if (text.toInt() <= 0) error("Phải có ít nhất 1 cột điểm")
                                                    else null
                                                } else error("Number is required")
                                            else null
                                        }
                                    }
                                }

                                hbox(spacing = 40) {
                                    field("Giờ học từ *") {
                                        this += JFXTimePicker().apply {
                                            bind(classModel.fromHour)
                                            defaultColor = c("#3f51b5")
                                            isOverLay = false

                                            required()
                                        }
                                    }

                                    field("Đến *") {
                                        this += JFXTimePicker().apply {
                                            bind(classModel.toHour)
                                            defaultColor = c("#3f51b5")
                                            isOverLay = false

                                            required()
                                        }
                                    }
                                }

                                hbox(spacing = 40) {
                                    field("Ngày khai giảng *") {
                                        this += JFXDatePicker().apply {
                                            bind(classModel.startDate)

                                            defaultColor = c("#3f51b5")
                                            isOverLay = false

                                            required()
                                        }
                                    }

                                    field("Số tháng *") {
                                        this += JFXTextField().apply {
                                            bind(month)
                                            bind(classModel.monthPeriods)
                                            required()
                                            validator { text ->
                                                when {
                                                    text != null && text.isInt() && text.toInt() == 0 -> error("Khóa học tối thiểu 1 tháng")
                                                    else -> null
                                                }
                                            }
                                        }
                                    }
                                }

                                field("Nội dung") {
                                    this += JFXTextArea().apply {
                                        bind(classModel.description)
                                    }
                                }
                            }
                        }

                        hbox {
                            alignment = Pos.BOTTOM_RIGHT
                            spacing = 20.0

                            this += JFXButton("Hủy bỏ").apply {
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED
                                paddingVertical = 15
                                paddingHorizontal = 30

                                action { modalStage?.close() }

                                enableWhen(isProcessing.not())

                                style {
                                    backgroundColor += c("#ff5533")
                                    textFill = c("#fff")
                                }
                            }

                            this += JFXButton("Hoàn tất").apply {
                                useMaxWidth = true
                                buttonType = JFXButton.ButtonType.RAISED
                                paddingVertical = 15
                                paddingHorizontal = 30

                                enableWhen(classModel.dirty.and(classModel.valid).and(isProcessing.not()))

                                action {
                                    isProcessing.value = true
                                    StudentManagerApplication.startSync()
                                    // base on the crud mode, we define the appropriate action
                                    runAsync {
                                        when (mode) {
                                            SMCRUDUtils.CRUDMode.NEW -> serviceCentral.classService.createNewClass(classModel)
                                            SMCRUDUtils.CRUDMode.EDIT -> serviceCentral.classService.editClass(classModel)
                                            else -> {
                                                SMCRUDUtils.SMCRUDResult(false, "Unsupported CRUD mode")
                                            }
                                        }
                                    } ui {
                                        isProcessing.value = false
                                        StudentManagerApplication.stopSync()
                                        // refresh if success
                                        if (it.success) {
                                            fire(SMClassListRefreshRequest())

                                            modalStage?.close()
                                        } else {
                                            error("Đã xảy ra lỗi", it.errorMessage, ButtonType.CLOSE)
                                        }
                                    }
                                }

                                style {
                                    backgroundColor += c("#ffffff")
                                }
                            }
                        }
                    }
                }
            }

            if (mode != SMCRUDUtils.CRUDMode.NEW) {
                /**
                 * Method to build a search box to allow adding new student into class
                 *
                 * @author ebolo
                 *
                 * @receiver EventTarget
                 */
                fun EventTarget.addStudentForm() {
                    this.form {
                        fieldset(labelPosition = Orientation.VERTICAL) {
                            field("Thêm học viên") {
                                // Text field to search for the student
                                this += JFXTextField().apply studentSearchTextField@{
                                    val autoCompletePopup = JFXAutoCompletePopup<SMStudentModel.SMStudentDto>().apply {
                                        fixedCellSize = 36.0

                                        suggestions.addAll(studentList)
                                        setSelectionHandler { event ->
                                            // When select, set the text field to be empty
                                            // add the student into the student list of the class
                                            val chosenStudent = event.getObject()
                                            this@studentSearchTextField.text = ""
                                            //classModel.item.studentList.add(chosenStudent)

                                            // Register this student into class
                                            runAsync {
                                                with(serviceCentral.classService) {
                                                    chosenStudent registerToClass classModel
                                                }
                                            }
                                        }

                                        // Make the cells in the auto-complete popup to show the student's name
                                        setSuggestionsCellFactory {
                                            object : JFXListCell<SMStudentModel.SMStudentDto>() {
                                                override fun updateItem(item: SMStudentModel.SMStudentDto?, empty: Boolean) {
                                                    super.updateItem(item, empty)

                                                    if (!empty) {
                                                        text = "${item!!.lastName} ${item.firstName}"
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Filter out the students in the return list
                                    textProperty().addListener { _, _, _ ->
                                        autoCompletePopup.filter { chosenStudent ->
                                            val tokens = this.text
                                                .split(' ')
                                                .filter { it.isNotBlank() }
                                                .map { StringUtils.stripAccents(it).toLowerCase() }

                                            !classModel.item.studentList.any { student ->
                                                student.id == chosenStudent.id
                                            } // This student has not already been in the class
                                                && (tokens.isEmpty() || tokens.any {
                                                StringUtils.stripAccents(chosenStudent.firstName).toLowerCase().contains(it)
                                                    || StringUtils.stripAccents(chosenStudent.lastName).toLowerCase().contains(it)
                                                    || StringUtils.stripAccents(chosenStudent.nickname).toLowerCase().contains(it)
                                            })
                                        }

                                        if (autoCompletePopup.filteredSuggestions.isEmpty() || this.text.isEmpty()) {
                                            autoCompletePopup.hide()
                                            // if you remove textField.getText.isEmpty()
                                            // when text field is empty it suggests all options
                                            // so you can choose
                                        } else {
                                            autoCompletePopup.show(this)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                tab("Điểm số") {
                    borderpane {
                        // Top is a search box to search for the student to add into the class
                        top {
                            addStudentForm()
                        }

                        center = find<SMClassStudentPerformanceListFragment>(
                            "classModel" to classModel
                        ).root
                    }
                }

                tab("Điểm danh") {
                    borderpane {
                        top {
                            addStudentForm()
                        }

                        center = find<SMClassStudentAttendanceListFragment>(
                            "classModel" to classModel
                        ).root
                    }
                }

                tab("Học phí") {
                    borderpane {
                        top {
                            addStudentForm()
                        }

                        center = find<SMClassStudentTuitionFeeListFragment>(
                            "classModel" to classModel
                        ).root
                    }
                }
            }
        }
    }
}