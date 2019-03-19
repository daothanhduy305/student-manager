package com.ebolo.studentmanager.models

import com.ebolo.common.database.entities.EboloBaseEntity
import tornadofx.*
import java.time.Instant

abstract class SMBaseModel<E : EboloBaseEntity, D : SMBaseModel.SMBaseDto> : ItemViewModel<D>() {

    abstract class SMBaseDto {
        var id by property<String>()
        fun idProperty() = getProperty(SMBaseDto::id)

        var createdTime by property<Instant>()
        fun createdTimeProperty() = getProperty(SMBaseDto::createdTime)

        var lastModifiedTime by property<Instant>()
        fun lastModifiedTimeProperty() = getProperty(SMBaseDto::lastModifiedTime)

        var createdBy by property<String>()
        fun createdByProperty() = getProperty(SMBaseDto::createdBy)

        var lastModifiedBy by property<String>()
        fun lastModifiedByProperty() = getProperty(SMBaseDto::lastModifiedBy)
    }

    val id = bind(SMBaseDto::idProperty)
    val createdTime = bind(SMBaseDto::createdTimeProperty)
    val createdBy = bind(SMBaseDto::createdByProperty)
    val lastModifiedTime = bind(SMBaseDto::lastModifiedTimeProperty)
    val lastModifiedBy = bind(SMBaseDto::lastModifiedByProperty)

    abstract fun getEntity(): E
}