package com.ebolo.studentmanager.services

import tornadofx.*
import javax.annotation.PostConstruct

/**
 * Service to handle general UI functions
 */
@org.springframework.stereotype.Controller
class SMUIService : Controller() {
    @PostConstruct
    fun setup() {
        subscribe<SMDataProcessRequest> { request ->
            request.processFunction()
        }
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