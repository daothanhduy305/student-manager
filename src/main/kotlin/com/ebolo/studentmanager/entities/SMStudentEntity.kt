package com.ebolo.studentmanager.entities

import org.springframework.data.mongodb.core.mapping.Document

@Document("Students")
class SMStudentEntity(
    var nickname: String = "",
    var parentPhone: String? = null,
    var educationLevel: EducationLevel = EducationLevel.NONE
) : SMUserEntity()

enum class EducationLevel {
    PRE_SCHOOL,
    PRIMARY,
    SECONDARY,
    HIGH_SCHOOL,
    UNDERGRADUATE,
    GRADUATED,
    WORKING,
    NONE
}