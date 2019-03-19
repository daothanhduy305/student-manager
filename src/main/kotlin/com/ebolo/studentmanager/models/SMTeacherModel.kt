package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMTeacherEntity
import tornadofx.*

class SMTeacherModel : SMBaseModel<SMTeacherEntity, SMTeacherModel.SMTeacherDto>() {

    // region dto
    class SMTeacherDto : SMBaseDto() {
        var firstName by property<String>()
        fun firstNameProperty() = getProperty(SMTeacherDto::firstName)

        var lastName by property<String>()
        fun lastNameProperty() = getProperty(SMTeacherDto::lastName)
    }
    // endregion

    // region bindings
    val firstName = bind(SMTeacherDto::firstNameProperty)
    val lastName = bind(SMTeacherDto::lastNameProperty)
    // endregion

    override fun getEntity() = SMTeacherEntity()
}