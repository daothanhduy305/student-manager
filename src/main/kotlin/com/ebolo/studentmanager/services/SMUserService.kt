package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.ebolo.utils.getWhenPresentOr
import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.entities.SMUserEntity
import com.ebolo.studentmanager.models.SMUserModel
import com.ebolo.studentmanager.repositories.SMUserRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import tornadofx.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Service class to provide functionality over the user of this application
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property userRepository SMUserRepository
 * @property logger Logger
 * @constructor
 */
@Service
class SMUserService(
    private val userRepository: SMUserRepository
) : Controller() {
    private val logger = loggerFor(SMUserService::class.java)

    private val passwordEncoder: BCryptPasswordEncoder by lazy {
        BCryptPasswordEncoder()
    }

    private var smUserListRefreshRequestRegistration by singleAssign<FXEventRegistration>()

    @PostConstruct
    fun setupSubscriptions() {
        // register the student list refresh request and event
        smUserListRefreshRequestRegistration = subscribe<SMUserListRefreshRequest> {
            runLater {
                StudentManagerApplication.startSync()

                runAsync {
                    val userList = getUserList()
                    fire(SMUserListRefreshEvent(userList))
                }
            }
        }
    }

    /**
     * Method to shut down this service
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down user service")
        smUserListRefreshRequestRegistration.unsubscribe()
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
        .findAllByDisabledFalse()
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
     * Method to delete a user from the system
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @param userModel SMUserModel
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun deleteUser(userModel: SMUserModel): SMCRUDUtils.SMCRUDResult = userRepository.findById(userModel.id.value)
        .getWhenPresentOr(
            ifPresentHandler = {
                userRepository.deleteById(userModel.id.value)
                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false, "Người dùng không tồn tại")
            }
        )

    /**
     * Method to delete a list of users
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @param idList List<String>
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun deleteUsers(idList: List<String>): SMCRUDUtils.SMCRUDResult = try {
        logger.info("Deleting user(s) '${idList.joinToString()}'")
        userRepository.saveAll(userRepository.findAllByIdInAndDisabledFalse(idList).map {
            it.apply { disabled = true }
        })

        SMCRUDUtils.SMCRUDResult(true)
    } catch (ex: Exception) {
        logger.error(ex.message, ex)
        SMCRUDUtils.SMCRUDResult(
            false, ex.message ?: "Something went wrong while deleting Class(es) '${idList.joinToString()}'")
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

        preferences {
            // Get the master account info
            val masterUsername = get(Settings.MASTER_ACCOUNT_USERNAME, "")
            val masterPassword = get(Settings.MASTER_ACCOUNT_PASSWORD, "")

            if (user.username == masterUsername && passwordEncoder.matches(user.password, masterPassword)) {
                authenticated = true
                putBoolean(Settings.GOD_MODE, true)
            } else {
                val userInDB = userRepository.findByUsername(user.username)

                if (userInDB.isPresent) {
                    authenticated = passwordEncoder.matches(user.password, userInDB.get().password)
                }
            }

            if (authenticated) {
                put(Settings.LOGGING_USER, user.username)
            }
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
        preferences {
            listOf(
                Settings.REMEMBER_CREDENTIAL,
                Settings.CREDENTIAL_USERNAME,
                Settings.CREDENTIAL_PASSWORD,
                Settings.LOGGING_USER,
                Settings.GOD_MODE
            ).forEach { remove(it) }
        }
        return true
    }

    /**
     * Method to check if there has been an authenticated user in the app's cache
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @return Boolean
     */
    fun checkCurrentUserAuthentication(): Boolean {
        var authenticated = false

        preferences {
            if (getBoolean(Settings.REMEMBER_CREDENTIAL, false)) {
                // If the remember has been ticked then check the saved credential
                val savedUsername = get(Settings.CREDENTIAL_USERNAME, "")
                val savedPassword = get(Settings.CREDENTIAL_PASSWORD, "")

                if (savedUsername.isNotEmpty() && savedPassword.isNotEmpty()) {
                    if (login(SMUserEntity().apply {
                            this.username = savedUsername
                            this.password = savedPassword
                        })) {

                        authenticated = true
                    } else {
                        listOf(
                            Settings.CREDENTIAL_USERNAME,
                            Settings.CREDENTIAL_PASSWORD,
                            Settings.REMEMBER_CREDENTIAL
                        ).forEach { remove(it) }
                    }
                } else {
                    remove(Settings.REMEMBER_CREDENTIAL)
                }
            }
        }

        return authenticated
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