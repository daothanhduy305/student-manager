package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseEntity
import com.ebolo.common.database.repositories.mongo.CascadeSave
import com.ebolo.common.utils.reflect.copyProperties
import com.ebolo.studentmanager.models.SMClassModel
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import tornadofx.*
import java.time.Instant
import java.time.ZoneOffset

/**
 * Entity class for the class data
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property name String
 * @property teacher SMTeacherEntity
 * @property subject SMSubjectEntity
 * @property startDate Instant?
 * @property studentPerformanceList MutableList<SMStudentPerformanceInfo>
 * @property studentList MutableSet<SMStudentEntity>
 * @property numberOfExams Int
 * @property tuitionFee Int
 * @constructor
 */
@Document("Classes")
class SMClassEntity(
    var name: String = "",
    @DBRef
    @CascadeSave
    var teacher: SMTeacherEntity = SMTeacherEntity(),
    @DBRef
    @CascadeSave
    var subject: SMSubjectEntity = SMSubjectEntity(),
    var startDate: Instant? = null,
    var studentPerformanceList: MutableList<SMStudentPerformanceInfo> = mutableListOf(),
    @DBRef
    @CascadeSave
    var studentList: MutableList<SMStudentEntity> = mutableListOf(),
    var numberOfExams: Int = 0,
    var tuitionFee: Int = 0
) : EboloBaseEntity(), SMIEntity<SMClassModel.SMClassDto> {

    override fun toDto(): SMClassModel.SMClassDto = this.copyProperties(
        destination = SMClassModel.SMClassDto(),
        preProcessedValues = mapOf(
            "startDate" to this.startDate?.atOffset(ZoneOffset.UTC)?.toLocalDate(),
            "teacher" to this.teacher.toDto(),
            "subject" to this.subject.toDto(),
            "studentList" to this.studentList.map {
                it.toDto()
            }.observable(),
            "studentPerformanceList" to this.studentPerformanceList.observable()
        )
    )
}

/**
 * Class to represent the student's performance per class
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property student String
 * @property note String
 * @property results MutableList<Int>
 * @constructor
 */
class SMStudentPerformanceInfo(
    var student: String = "",
    var note: String = "",
    var results: MutableList<Int> = mutableListOf(),
    var startDate: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        return other is SMStudentPerformanceInfo && other.student == this.student
    }

    override fun hashCode(): Int {
        return student.hashCode()
    }
}