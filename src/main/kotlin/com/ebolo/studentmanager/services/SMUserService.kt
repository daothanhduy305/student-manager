package com.ebolo.studentmanager.services

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.entities.SMUserEntity
import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.repositories.SMUserRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import tornadofx.*
import javax.annotation.PostConstruct

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
    private val userRepository: SMUserRepository,
    private val cacheService: SMCacheService,
    private val passwordEncoder: BCryptPasswordEncoder
) : Controller() {
    private val logger = loggerFor(SMUserService::class.java)

    @PostConstruct
    fun setupSubscriptions() {
        // register the student list refresh request and event
        subscribe<SMUserListRefreshRequest> {
            fire(SMUserListRefreshEvent(getUserList()))
        }
    }

    /**
     * Method to retrieve the user list available in the system
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @return List<SMUserModel.SMUserDto>
     */
    fun getUserList(): List<SMUserModel.SMUserDto> = userRepository
        .findAll()
        .map {
            it.toDto()
        }

    /**
     * Method check if the db has already contained the user or not and add new if not
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param userModel SMUserModel model contains info of the user
     * @return SMCRUDUtils
     */
    fun createNewOrUpdateUser(userModel: SMUserModel): SMCRUDUtils.SMCRUDResult {
        val added = userRepository.findByUsername(userModel.username.value).isPresent

        if (!added) {
            userRepository.save(userModel.getEntity().apply {
                password = passwordEncoder.encode(password)
            })
            fire(SMUserListRefreshRequest)
        }

        return SMCRUDUtils.SMCRUDResult(
            success = !added,
            errorMessage = if (added) "Người dùng đã có trong cơ sở dữ liệu" else ""
        )
    }

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
        var authenticated = false

        logger.info("Logging user: ${user.username} into the system")

        // Get the master account info
        val masterUsername = cacheService.cache[Settings.MASTER_ACCOUNT_USERNAME] as String
        val masterPassword = cacheService.cache[Settings.MASTER_ACCOUNT_PASSWORD] as String

        if (user.username == masterUsername && passwordEncoder.matches(user.password, masterPassword)) {
            authenticated = true
        } else {
            val userInDB = userRepository.findByUsername(user.username)

            if (userInDB.isPresent) {
                authenticated = passwordEncoder.matches(user.password, userInDB.get().password)
            }
        }

        if (authenticated) {
            cacheService.setSettings(SMGlobal.CACHE_ENTRY_LOGGING_USER to user.username)
        }

        return authenticated
    }

    /**
     * Method to log the user out of the system
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @return Boolean
     */
    fun logout(): Boolean {
        logger.info("Logging out...")
        cacheService.removeSettings(
            Settings.REMEMBER_CREDENTIAL,
            Settings.CREDENTIAL_USERNAME,
            Settings.CREDENTIAL_PASSWORD,
            SMGlobal.CACHE_ENTRY_LOGGING_USER
        )
        return true
    }
}

/**
 * Request to refresh the user list when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
object SMUserListRefreshRequest : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the user list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property users List<SMUserDto>
 * @constructor
 */
class SMUserListRefreshEvent(val users: List<SMUserModel.SMUserDto>) : FXEvent()