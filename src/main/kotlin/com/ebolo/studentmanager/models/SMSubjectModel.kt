package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMSubjectEntity
import tornadofx.*

class SMSubjectModel : SMBaseModel<SMSubjectEntity, SMSubjectModel.SMSubjectDto>(SMSubjectEntity::class) {

    // region dto
    class SMSubjectDto : SMBaseDto() {
        var name by property<String>()
        fun nameProperty() = getProperty(SMSubjectDto::name)
    }
    // endregion

    // region bindings
    val name = bind(SMSubjectDto::nameProperty)
    // endregion

    override fun specificEntitySetup(entity: SMSubjectEntity) {
        entity.name = name.value
    }
}