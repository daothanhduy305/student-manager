package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.EducationLevel
import com.ebolo.studentmanager.entities.SMStudentEntity
import tornadofx.*
import java.time.Instant

class SMStudentModel : SMBaseModel<SMStudentEntity, SMStudentModel.SMStudentDto>() {

    // region dto
    class SMStudentDto : SMBaseDto() {
        var firstName by property<String>()
        fun firstNameProperty() = getProperty(SMStudentDto::firstName)

        var lastName by property<String>()
        fun lastNameProperty() = getProperty(SMStudentDto::lastName)

        var nickname by property<String>()
        fun nicknameProperty() = getProperty(SMStudentDto::nickname)

        var birthday by property<Instant?>()
        fun birthdayProperty() = getProperty(SMStudentDto::birthday)

        var phone by property<String?>()
        fun phoneProperty() = getProperty(SMStudentDto::phone)

        var parentPhone by property<String?>()
        fun parentPhoneProperty() = getProperty(SMStudentDto::parentPhone)

        var address by property<String?>()
        fun addressProperty() = getProperty(SMStudentDto::address)

        var joinedTime by property<Instant?>()
        fun joinedTimeProperty() = getProperty(SMStudentDto::joinedTime)

        var educationLevel by property<EducationLevel>()
        fun educationLevelProperty() = getProperty(SMStudentDto::educationLevel)
    }
    // endregion

    // region bindings
    val firstName = bind(SMStudentDto::firstNameProperty)
    val lastName = bind(SMStudentDto::lastNameProperty)
    val nickname = bind(SMStudentDto::nicknameProperty)
    val birthday = bind(SMStudentDto::birthdayProperty)
    val phone = bind(SMStudentDto::phoneProperty)
    val parentPhone = bind(SMStudentDto::parentPhoneProperty)
    val address = bind(SMStudentDto::addressProperty)
    val joinedTime = bind(SMStudentDto::joinedTimeProperty)
    val educationLevel = bind(SMStudentDto::educationLevelProperty)
    // endregion

    override fun getEntity() = SMStudentEntity().also {
        it.id = id.value

        it.firstName = firstName.value
        it.lastName = lastName.value
        it.nickname = nickname.value
        it.birthday = birthday.value?.value
        it.phone = phone.value?.value
        it.parentPhone = parentPhone.value?.value
        it.address = address.value?.value
        it.joinedTime = joinedTime.value?.value
        it.educationLevel = educationLevel.value
    }
}