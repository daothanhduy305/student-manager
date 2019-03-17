package com.ebolo.studentmanager.entities

import org.springframework.data.mongodb.core.mapping.Document

@Document("Students")
class SMStudentEntity : SMUserEntity()