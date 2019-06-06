package com.ebolo.studentmanager.services

import com.ebolo.common.utils.loggerFor
import tornadofx.Controller
import tornadofx.FXEvent
import tornadofx.FXEventRegistration
import tornadofx.singleAssign
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Service to handle general UI functions
 */
@org.springframework.stereotype.Controller
class SMUIService : Controller() {
    private val logger = loggerFor(this::class.java)
    private var smDataProcessRequestRegistration by singleAssign<FXEventRegistration>()

    @PostConstruct
    fun setup() {
        smDataProcessRequestRegistration = subscribe<SMDataProcessRequest> { request ->
            request.processFunction()
        }
    }

    /**
     * Method to shut down this service
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down UI service")
        unsubscribe<SMDataProcessRequest> { smDataProcessRequestRegistration.action }
    }
}

/**
 * Request to be fired to request a data processing block. Be cautious as this will be fired on Application Thread
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @constructor
 */
class SMDataProcessRequest(val processFunction: () -> Unit) : FXEvent()

/**
 * Request to be fired to make the application to reload the context
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
object SMRestartAppRequest : FXEvent()