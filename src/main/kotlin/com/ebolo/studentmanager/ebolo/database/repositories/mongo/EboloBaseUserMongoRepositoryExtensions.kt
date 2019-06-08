package com.ebolo.studentmanager.ebolo.database.repositories.mongo

import com.ebolo.studentmanager.ebolo.database.entities.EboloBaseUserEntity

/**
 * Wrapper method for retrieving user info from either email, username or firebaseUid with that exact order of matching
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @receiver EboloBaseUserMongoRepository<S>
 * @param info String
 * @return S?
 */
fun <S : EboloBaseUserEntity> EboloBaseUserMongoRepository<S>.findByEmailOrUsernameOrFirebaseUidUnified(info: String) = this.findByEmailOrUsernameOrFirebaseUid(info, info, info)