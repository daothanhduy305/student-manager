package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Month
import java.time.Year

@Document("Attendance")
class SMAttendanceEntity(
    var studentId: String = "",
    var classId: String = "",
    var year: Int = Year.MIN_VALUE,
    var month: Month = Month.JANUARY,
    var day: Int = 1
) : EboloBaseEntity()