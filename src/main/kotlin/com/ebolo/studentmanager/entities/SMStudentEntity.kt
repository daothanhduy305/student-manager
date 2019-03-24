package com.ebolo.studentmanager.entities

import org.springframework.data.mongodb.core.mapping.Document

@Document("Students")
class SMStudentEntity(
    var nickname: String = "",
    var parentPhone: String? = null,
    var educationLevel: EducationLevel = EducationLevel.NONE
) : SMUserEntity()

enum class EducationLevel(val title: String) {
    NONE(""),
    PRE_SCHOOL("Mẫu giáo"),
    PRIMARY("Tiểu học"),
    SECONDARY("Trung học cơ sở"),
    HIGH_SCHOOL("Trung học phổ thông"),
    UNDERGRADUATE("Sinh viên"),
    GRADUATED("Cử nhân"),
    WORKING("Đang đi làm")
}