package com.ebolo.studentmanager.ebolo.database.repositories.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CascadeSave

class EboloCascadeCallback(
    var source: Any?,
    mongoOperations: MongoOperations
) : ReflectionUtils.FieldCallback {
    var mongoOperations: MongoOperations? = null

    init {
        this.mongoOperations = mongoOperations
    }

    @Throws(IllegalArgumentException::class, IllegalAccessException::class)
    override fun doWith(field: Field) {
        ReflectionUtils.makeAccessible(field)

        if (field.isAnnotationPresent(DBRef::class.java) && field.isAnnotationPresent(CascadeSave::class.java)) {
            val fieldValue = field.get(source)

            val insta = fieldValue !is String
            val instanull = fieldValue != null

            if (instanull && insta) {
                val callback = EboloFieldCallback()
                ReflectionUtils.doWithFields(fieldValue!!.javaClass, callback)
                mongoOperations!!.save(fieldValue)
            }
        }
    }
}

class EboloCascadeSaveMongoEventListener(
    private val mongoOperations: MongoOperations
) : AbstractMongoEventListener<Any>() {

    fun onBeforeConvert(source: Any) {
        ReflectionUtils.doWithFields(
            source.javaClass,
            EboloCascadeCallback(source, mongoOperations))
    }
}

class EboloFieldCallback : ReflectionUtils.FieldCallback {
    var isIdFound: Boolean = false
        private set

    @Throws(IllegalArgumentException::class, IllegalAccessException::class)
    override fun doWith(field: Field) {
        ReflectionUtils.makeAccessible(field)

        if (field.isAnnotationPresent(Id::class.java)) {
            isIdFound = true
        }
    }
}