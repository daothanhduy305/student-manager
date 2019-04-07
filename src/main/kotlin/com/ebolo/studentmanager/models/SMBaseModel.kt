package com.ebolo.studentmanager.models

import com.ebolo.common.database.entities.EboloBaseEntity
import tornadofx.*
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class SMBaseModel<E : EboloBaseEntity, D : SMBaseModel.SMBaseDto>(
    val entityClass: KClass<E>,
    item: D? = null
) : ItemViewModel<D>(item) {

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

    /**
     * Method to create an entity object from the current model state
     *
     * @author ebolo(daothanhduy305@gmail.com)
     * @since 0.0.1 - SNAPSHOT
     *
     * @return E
     */
    fun getEntity(): E = entityClass.createInstance().also { entity ->
        // set the stamps up
        Optional.ofNullable(id.value).ifPresent { entity.id = id.value }
        Optional.ofNullable(createdBy.value).ifPresent { entity.createdBy = createdBy.value }
        Optional.ofNullable(createdTime.value).ifPresent { entity.createdTime = createdTime.value }
        Optional.ofNullable(lastModifiedBy.value).ifPresent { entity.lastModifiedBy = lastModifiedBy.value }
        Optional.ofNullable(lastModifiedTime.value).ifPresent { entity.lastModifiedTime = lastModifiedTime.value }

        specificEntitySetup(entity)
    }

    /**
     * Method to be overridden by the inheritance to handle specific property setup for the returning entity
     *
     * @author ebolo(daothanhduy305@gmail.com)
     * @since 0.0.1 - SNAPSHOT
     *
     * @param entity E
     */
    abstract fun specificEntitySetup(entity: E)
}