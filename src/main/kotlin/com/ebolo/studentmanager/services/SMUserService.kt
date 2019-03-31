package com.ebolo.studentmanager.services

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.entities.SMUserEntity
import com.ebolo.studentmanager.repositories.SMUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service class to provide functionality over the user of this application
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property userRepository SMUserRepository
 * @property cacheService SMCacheService
 * @property logger Logger
 * @constructor
 */
@Service
class SMUserService(
    @Autowired private val userRepository: SMUserRepository,
    @Autowired private val cacheService: SMCacheService
) {
    private val logger = loggerFor(SMUserService::class.java)

    /**
     * Method to log a user credential into the system
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param user SMUserEntity
     * @return Boolean
     */
    fun login(user: SMUserEntity): Boolean {
        logger.info("Logging user: ${user.username} into the system")
        cacheService.cache[SMGlobal.CACHE_ENTRY_LOGGING_USER] = user.username
        return true
    }
}