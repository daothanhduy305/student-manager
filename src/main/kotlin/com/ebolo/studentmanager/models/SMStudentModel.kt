package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMStudentEntity
import tornadofx.ItemViewModel
import tornadofx.getProperty
import tornadofx.property

class SMStudentModel : SMBaseModel<SMStudentEntity>, ItemViewModel<SMStudentModel.SMStudentDto>() {

    // region dto
    class SMStudentDto {
        var firstName by property<String>()
        fun firstNameProperty() = getProperty(SMStudentDto::firstName)

        var lastName by property<String>()
        fun lastNameProperty() = getProperty(SMStudentDto::lastName)
    }
    // endregion

    // region bindings
    val firstName = bind(SMStudentDto::firstNameProperty)
    val lastName = bind(SMStudentDto::lastNameProperty)
    // endregion

    override fun getEntity() = SMStudentEntity().also {
        it.firstName = firstName.value ?: ""
        it.lastName = lastName.value ?: ""
    }
}