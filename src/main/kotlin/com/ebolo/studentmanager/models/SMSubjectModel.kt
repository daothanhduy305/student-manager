package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMSubjectEntity
import tornadofx.*

class SMSubjectModel : SMBaseModel<SMSubjectEntity, SMSubjectModel.SMSubjectDto>() {

    // region dto
    class SMSubjectDto : SMBaseDto() {
        var name by property<String>()
        fun nameProperty() = getProperty(SMSubjectDto::name)
    }
    // endregion

    // region bindings
    val name = bind(SMSubjectDto::nameProperty)
    // endregion

    override fun getEntity() = SMSubjectEntity().also {
        it.name = name.value
        it.id = id.value
    }
}