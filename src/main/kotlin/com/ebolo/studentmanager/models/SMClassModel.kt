package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMClassEntity
import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import javafx.collections.FXCollections
import tornadofx.*
import java.time.LocalDate
import java.time.ZoneOffset

class SMClassModel(
    item: SMClassDto? = null
) : SMBaseModel<SMClassEntity, SMClassModel.SMClassDto>(SMClassEntity::class, item) {
    // region dto
    class SMClassDto : SMBaseDto() {
        var name by property<String>()
        fun nameProperty() = getProperty(SMClassDto::name)

        var teacher by property<SMTeacherModel.SMTeacherDto>()
        fun teacherProperty() = getProperty(SMClassDto::teacher)

        var subject by property<SMSubjectModel.SMSubjectDto>()
        fun subjectProperty() = getProperty(SMClassDto::subject)

        var tuitionFee by property<Number>()
        fun tuitionFeeProperty() = getProperty(SMClassDto::tuitionFee)

        var numberOfExams by property<Number>()
        fun numberOfExamsProperty() = getProperty(SMClassDto::numberOfExams)

        var startDate by property<LocalDate>()
        fun startDateProperty() = getProperty(SMClassDto::startDate)

        var studentList = FXCollections.emptyObservableList<SMStudentModel.SMStudentDto>()

        var studentPerformanceList = FXCollections.emptyObservableList<SMStudentPerformanceInfo>()
    }
    // endregion

    // region bindings
    val name = bind(SMClassDto::nameProperty)
    val teacher = bind(SMClassDto::teacherProperty)
    val subject = bind(SMClassDto::subjectProperty)
    val tuitionFee = bind(SMClassDto::tuitionFeeProperty)
    val numberOfExams = bind(SMClassDto::numberOfExamsProperty)
    val startDate = bind(SMClassDto::startDateProperty)
    val studentList = bind(SMClassDto::studentList)
    val studentPerformanceList = bind(SMClassDto::studentPerformanceList)
    // endregion

    override fun specificEntitySetup(entity: SMClassEntity) {
        entity.name = this.name.value
        entity.numberOfExams = this.numberOfExams.value.toInt()
        entity.tuitionFee = this.tuitionFee.value.toInt()
        entity.startDate = this.startDate.value.atStartOfDay().toInstant(ZoneOffset.UTC)
        entity.subject = SMSubjectModel().apply { item = subject.value }.getEntity()
        entity.teacher = SMTeacherModel().apply { item = teacher.value }.getEntity()
        entity.studentList = this.studentList.value
            .map { dto -> SMStudentModel().apply { item = dto }.getEntity() }
            .toMutableList()
        entity.studentPerformanceList = this.studentPerformanceList.value.toMutableList()
    }
}