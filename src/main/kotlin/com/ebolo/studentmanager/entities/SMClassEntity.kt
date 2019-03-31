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
 * @property teacher String
 * @property startDate Date?
 * @property studentPerformanceList MutableList<SMStudentPerformanceInfo>
 * @property numberOfExams Int
 * @constructor
 */
@Document("Classes")
class SMClassEntity(
    var name: String = "",
    @DBRef(lazy = true) var teacher: SMTeacherEntity = SMTeacherEntity(),
    var subject: String = "",
    var startDate: Instant? = null,
    var studentPerformanceList: MutableList<SMStudentPerformanceInfo> = mutableListOf(),
    @DBRef(lazy = true) var studentList: MutableSet<SMStudentEntity> = mutableSetOf(),
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