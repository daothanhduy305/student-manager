package com.ebolo.studentmanager.ebolo.database.repositories.mongo

import com.ebolo.studentmanager.ebolo.database.entities.EboloBaseUserEntity
import java.util.*

interface EboloBaseUserMongoRepository<T : EboloBaseUserEntity> : EboloBaseMongoRepository<T> {
    fun findByEmail(email: String): Optional<T>
    fun findByUsername(username: String): Optional<T>
    fun findByFirebaseUid(uid: String): Optional<T>
    fun findByEmailOrUsernameOrFirebaseUid(email: String, username: String, firebaseUid: String): Optional<T>
}