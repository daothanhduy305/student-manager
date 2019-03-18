package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMSubjectEntity
import tornadofx.*

class SMSubjectModel : SMBaseModel<SMSubjectEntity>, ItemViewModel<SMSubjectModel.SMSubjectDto>() {

    // region dto
    class SMSubjectDto {
        var id by property<String>()
        fun idProperty() = getProperty(SMSubjectDto::id)

        var name by property<String>()
        fun nameProperty() = getProperty(SMSubjectDto::name)
    }
    // endregion

    // region bindings
    val id = bind(SMSubjectDto::idProperty)
    val name = bind(SMSubjectDto::nameProperty)
    // endregion

    override fun getEntity() = SMSubjectEntity().also {
        it.name = name.value
        it.id = id.value
    }
}