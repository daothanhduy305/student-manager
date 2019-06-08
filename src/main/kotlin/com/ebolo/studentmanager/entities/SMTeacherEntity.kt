package com.ebolo.studentmanager.entities

import com.ebolo.studentmanager.ebolo.database.entities.EboloBaseUserEntity
import com.ebolo.studentmanager.ebolo.utils.copyProperties
import com.ebolo.studentmanager.models.SMTeacherModel
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZoneOffset

@Document("Teachers")
class SMTeacherEntity : EboloBaseUserEntity(), SMIEntity<SMTeacherModel.SMTeacherDto> {

    override fun toDto(): SMTeacherModel.SMTeacherDto = this.copyProperties(
        destination = SMTeacherModel.SMTeacherDto(),
        preProcessedValues = mapOf(
            // pre-process the birthday since we must use LocalDate for the model - for datepicker
            "birthday" to (
                if (this.birthday != null)
                    this.birthday!!.atOffset(ZoneOffset.UTC).toLocalDate()
                else
                    null
                )
        )
    )
}