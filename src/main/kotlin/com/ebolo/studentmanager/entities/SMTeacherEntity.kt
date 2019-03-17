package com.ebolo.studentmanager.entities

import org.springframework.data.mongodb.core.mapping.Document

@Document("Teachers")
class SMTeacherEntity : SMUserEntity()