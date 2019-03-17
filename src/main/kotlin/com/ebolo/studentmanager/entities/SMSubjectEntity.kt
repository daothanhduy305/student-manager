package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseEntity
import org.springframework.data.mongodb.core.mapping.Document

@Document("Subjects")
class SMSubjectEntity(
    var name: String = ""
) : EboloBaseEntity()