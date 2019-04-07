package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseEntity
import com.ebolo.common.utils.reflect.unsafeCastTo
import com.ebolo.studentmanager.models.SMSubjectModel
import org.springframework.data.mongodb.core.mapping.Document

@Document("Subjects")
class SMSubjectEntity(
    var name: String = ""
) : EboloBaseEntity(), SMIEntity<SMSubjectModel.SMSubjectDto> {

    override fun toDto(): SMSubjectModel.SMSubjectDto = this unsafeCastTo SMSubjectModel.SMSubjectDto::class
}