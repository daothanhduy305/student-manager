package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMTeacherEntity
import tornadofx.*
import java.time.LocalDate
import java.time.ZoneOffset

class SMTeacherModel(
    item: SMTeacherDto? = null
) : SMBaseModel<SMTeacherEntity, SMTeacherModel.SMTeacherDto>(SMTeacherEntity::class, item) {

    // region dto
    class SMTeacherDto : SMBaseDto() {
        var firstName by property<String>()
        fun firstNameProperty() = getProperty(SMTeacherDto::firstName)

        var lastName by property<String>()
        fun lastNameProperty() = getProperty(SMTeacherDto::lastName)

        var phone by property<String>()
        fun phoneProperty() = getProperty(SMTeacherDto::phone)

        var address by property<String>()
        fun addressProperty() = getProperty(SMTeacherDto::address)

        var birthday by property<LocalDate>()
        fun birthdayProperty() = getProperty(SMTeacherDto::birthday)

        override fun toString(): String {
            return "$lastName $firstName"
        }
    }
    // endregion

    // region bindings
    val firstName = bind(SMTeacherDto::firstNameProperty)
    val lastName = bind(SMTeacherDto::lastNameProperty)
    val phone = bind(SMTeacherDto::phoneProperty)
    val address = bind(SMTeacherDto::addressProperty)
    val birthday = bind(SMTeacherDto::birthdayProperty)
    // endregion

    override fun specificEntitySetup(entity: SMTeacherEntity) {
        entity.firstName = firstName.value
        entity.lastName = lastName.value
        entity.birthday = birthday.value?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
        entity.phone = phone.value
        entity.address = address.value ?: ""
    }
}