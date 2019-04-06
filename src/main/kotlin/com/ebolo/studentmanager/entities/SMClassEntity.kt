package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseEntity
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

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
    @DBRef var teacher: SMTeacherEntity = SMTeacherEntity(),
    @DBRef var subject: SMSubjectEntity = SMSubjectEntity(),
    var startDate: Instant? = null,
    var studentPerformanceList: MutableList<SMStudentPerformanceInfo> = mutableListOf(),
    @DBRef var studentList: MutableSet<SMStudentEntity> = mutableSetOf(),
    var numberOfExams: Int = 0,
    var tuitionFee: Int = 0
) : EboloBaseEntity()

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
    var results: MutableList<Int> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        return other is SMStudentPerformanceInfo && other.student == this.student
    }

    override fun hashCode(): Int {
        return student.hashCode()
    }
}