package com.ebolo.studentmanager.ebolo.database.repositories.mongo

import com.ebolo.studentmanager.ebolo.database.entities.EboloBaseEntity
import org.springframework.data.mongodb.repository.MongoRepository

interface EboloBaseMongoRepository<T : EboloBaseEntity> : MongoRepository<T, String>