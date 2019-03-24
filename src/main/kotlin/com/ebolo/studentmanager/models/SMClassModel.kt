package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMClassEntity
import tornadofx.*

class SMClassModel : SMBaseModel<SMClassEntity, SMClassModel.SMClassDto>(SMClassEntity::class) {
    // region dto
    class SMClassDto : SMBaseDto() {
        var name by property<String>()
        fun nameProperty() = getProperty(SMClassDto::name)

        var teacher by property<String>()
        fun teacherProperty() = getProperty(SMClassDto::teacher)

        var subject by property<String>()
        fun subjectProperty() = getProperty(SMClassDto::subject)

        var tuitionFee by property<Int>()
        fun tuitionFeeProperty() = getProperty(SMClassDto::tuitionFee)
    }
    // endregion

    // region bindings
    val name = bind(SMClassDto::nameProperty)
    val teacher = bind(SMClassDto::teacherProperty)
    val subject = bind(SMClassDto::subjectProperty)
    val tuitionFee = bind(SMClassDto::tuitionFeeProperty)
    // endregion

    override fun specificEntitySetup(entity: SMClassEntity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}