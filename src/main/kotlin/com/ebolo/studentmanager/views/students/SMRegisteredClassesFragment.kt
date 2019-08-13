package com.ebolo.studentmanager.views.students

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.services.SMClassListRefreshEvent
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.views.classes.SMClassPerformanceFragment
import com.ebolo.studentmanager.views.utils.ui.tableview.handleItemsUpdated
import com.jfoenix.controls.JFXAutoCompletePopup
import com.jfoenix.controls.JFXListCell
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import org.apache.commons.lang3.StringUtils
import tornadofx.*
import java.time.LocalDate
import java.time.ZoneOffset

class SMRegisteredClassesFragment : Fragment() {
    private val serviceCentral: SMServiceCentral by di()

    private val studentModel: SMStudentModel by param(SMStudentModel())
    private val allClassesList by lazy { serviceCentral.classService.getClassList().asObservable() }
    private val classesList by lazy {
        allClassesList.filter { thisClass ->
            thisClass.studentList
                .any { student -> student.id == studentModel.id.value }
        }
            .toMutableList()
            .asObservable()
    }

    override val root = stackpane {
        borderpane {
            top {
                form {
                    fieldset(labelPosition = Orientation.VERTICAL) {
                        field("Thêm lớp") {
                            // Text field to search for the student
                            this += JFXTextField().apply studentSearchTextField@{
                                val autoCompletePopup = JFXAutoCompletePopup<SMClassModel.SMClassDto>().apply {
                                    fixedCellSize = 36.0

                                    suggestions.addAll(allClassesList)
                                    setSelectionHandler { event ->
                                        // When select, set the text field to be empty
                                        // add the student into the student list of the class
                                        val chosenClass = event.getObject()
                                        this@studentSearchTextField.text = ""
                                        //classModel.item.studentList.add(chosenStudent)

                                        // Register this student into class
                                        this@borderpane.runAsyncWithOverlay {
                                            with(serviceCentral.classService) {
                                                studentModel.item registerToClass SMClassModel(chosenClass)
                                            }
                                        }
                                    }

                                    // Make the cells in the auto-complete popup to show the student's name
                                    setSuggestionsCellFactory {
                                        object : JFXListCell<SMClassModel.SMClassDto>() {
                                            override fun updateItem(item: SMClassModel.SMClassDto?, empty: Boolean) {
                                                super.updateItem(item, empty)

                                                if (!empty) {
                                                    text = item!!.name
                                                }
                                            }
                                        }
                                    }
                                }

                                // Filter out the students in the return list
                                textProperty().addListener { _, _, _ ->
                                    autoCompletePopup.filter { chosenClass ->
                                        val tokens = this.text
                                            .split(' ')
                                            .filter { it.isNotBlank() }
                                            .map { StringUtils.stripAccents(it).toLowerCase() }

                                        !classesList.any { classInList ->
                                            classInList.id == chosenClass.id
                                        } // This student has not already been in the class
                                            && (tokens.isEmpty() || tokens.any {
                                            StringUtils.stripAccents(chosenClass.name).toLowerCase().contains(it)
                                                || StringUtils.stripAccents(chosenClass.teacher.firstName).toLowerCase().contains(it)
                                                || StringUtils.stripAccents(chosenClass.teacher.lastName).toLowerCase().contains(it)
                                                || StringUtils.stripAccents(chosenClass.subject.name).toLowerCase().contains(it)
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

            center {
                tableview(classesList) {
                    prefWidth = 600.0

                    makeIndexColumn("STT").apply {
                        style {
                            alignment = Pos.TOP_CENTER
                        }
                    }.prefWidth(75)

                    readonlyColumn("Tên lớp", SMClassModel.SMClassDto::name).prefWidth(150)

                    readonlyColumn("Giáo viên", SMClassModel.SMClassDto::teacher) {
                        cellFormat { teacher -> text = "${teacher.lastName} ${teacher.firstName}" }
                    }.prefWidth(150)

                    readonlyColumn("Môn", SMClassModel.SMClassDto::subject) {
                        cellFormat { subject -> text = subject.name }
                    }.prefWidth(100)

                    column<SMClassModel.SMClassDto, LocalDate>("Ngày bắt đầu", "studentStartDate") {
                        setCellValueFactory { cellData ->
                            val performanceInfo = cellData.value.studentPerformanceList.firstOrNull { it.student == studentModel.id.value }

                            if (performanceInfo != null)
                                SimpleObjectProperty(performanceInfo.startDate?.atOffset(ZoneOffset.UTC)?.toLocalDate())
                            else
                                SimpleObjectProperty<LocalDate>()
                        }
                    }.remainingWidth()

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
                                    "classInfo" to selectedItem!!)
                                    .openModal()
                            }
                        }

                        item("Xóa").action {
                            if (selectedItem != null) this@borderpane.runAsyncWithOverlay {
                                with(serviceCentral.classService) {
                                    studentModel.item deregisterFromClass SMClassModel(selectedItem!!)
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
                                    "classInfo" to selectedItem!!)
                                    .openModal()
                            }
                        }
                    }

                    // subscribe to events
                    subscribe<SMClassListRefreshEvent> { event ->
                        allClassesList.setAll(event.classes)
                        handleItemsUpdated(
                            allClassesList.filter { thisClass ->
                                thisClass.studentList
                                    .any { student -> student.id == studentModel.id.value }
                            },
                            classesList)
                    }
                }
            }
        }
    }
}
