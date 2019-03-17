package com.ebolo.studentmanager.services

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.entities.SMUserEntity
import com.ebolo.studentmanager.repositories.SMUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SMUserService(
    @Autowired private val userRepository: SMUserRepository,
    @Autowired private val cacheService: SMCacheService
) {
    private val logger = loggerFor(SMUserService::class.java)

    fun login(user: SMUserEntity): Boolean {
        logger.info("Logging user: ${user.username} into the system")
        cacheService.cache[SMGlobal.CACHE_ENTRY_LOGGING_USER] = user.username
        return true
    }

    fun validateLoginUserInfo(user: SMUserEntity): Boolean {
        return true
    }
}