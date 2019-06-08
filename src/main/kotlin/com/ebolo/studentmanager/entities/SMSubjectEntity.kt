package com.ebolo.studentmanager.entities

import com.ebolo.studentmanager.ebolo.database.entities.EboloBaseEntity
import com.ebolo.studentmanager.ebolo.utils.unsafeCastTo
import com.ebolo.studentmanager.models.SMSubjectModel
import org.springframework.data.mongodb.core.mapping.Document

@Document("Subjects")
class SMSubjectEntity(
    var name: String = ""
) : EboloBaseEntity(), SMIEntity<SMSubjectModel.SMSubjectDto> {

    override fun toDto(): SMSubjectModel.SMSubjectDto = this unsafeCastTo SMSubjectModel.SMSubjectDto::class
}