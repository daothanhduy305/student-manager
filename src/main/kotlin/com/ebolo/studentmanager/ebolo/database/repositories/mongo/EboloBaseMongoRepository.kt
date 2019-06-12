package com.ebolo.studentmanager.ebolo.database.repositories.mongo

import com.ebolo.studentmanager.ebolo.database.entities.EboloBaseEntity
import org.springframework.data.repository.CrudRepository

interface EboloBaseMongoRepository<T : EboloBaseEntity> : CrudRepository<T, String> {
    fun findAllByDisabledFalse(): List<T>
}