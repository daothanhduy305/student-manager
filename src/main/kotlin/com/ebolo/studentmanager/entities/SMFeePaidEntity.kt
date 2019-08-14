package com.ebolo.studentmanager.entities

import com.ebolo.studentmanager.ebolo.database.entities.EboloBaseEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.Month
import java.time.Year

@Document(collection = "FeePaid")
class SMFeePaidEntity(
    var studentId: String = "",
    var classId: String = "",
    var year: Int = Year.MIN_VALUE,
    var month: Month = Month.JANUARY,
    var paidDate: Instant? = null,
    var note: String = ""
) : EboloBaseEntity()