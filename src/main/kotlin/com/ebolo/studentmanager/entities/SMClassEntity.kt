package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

/**
 * Entity class for the class data
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property name String
 * @property teacher String
 * @property startDate Date?
 * @property studentList MutableList<SMStudentPerformanceInfo>
 * @property numberOfExams Int
 * @constructor
 */
@Document("Classes")
class SMClassEntity(
    var name: String = "",
    var teacher: String = "",
    var subject: String = "",
    var startDate: Date? = null,
    var studentList: MutableList<SMStudentPerformanceInfo> = mutableListOf(),
    var numberOfExams: Int = 0
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
)