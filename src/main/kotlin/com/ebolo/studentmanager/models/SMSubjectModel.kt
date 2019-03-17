package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMSubjectEntity
import tornadofx.ItemViewModel
import tornadofx.getProperty
import tornadofx.property

class SMSubjectModel : SMBaseModel<SMSubjectEntity>, ItemViewModel<SMSubjectModel.SMSubjectDto>() {

    // region dto
    class SMSubjectDto {
        var name by property<String>()
        fun nameProperty() = getProperty(SMSubjectDto::name)
    }
    // endregion

    // region bindings
    val name = bind(SMSubjectDto::nameProperty)
    // endregion

    override fun getEntity() = SMSubjectEntity().also {
        it.name = name.value ?: ""
    }
}