package com.ebolo.studentmanager.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import tornadofx.*

/**
 * This class serve as a central for all the available controllers within the system
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property userService SMUserService
 * @constructor
 */
@Service
class SMServiceCentral(
    val userService: SMUserService,
    val subjectService: SMSubjectService,
    val studentService: SMStudentService,
    val teacherService: SMTeacherService,
    val classService: SMClassService
) {
    @Value("\${version}")
    lateinit var version: String
}

/**
 * Request to be fired to make the application to reload the springContext
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
class SMRestartAppRequest(val restartMode: RestartMode = RestartMode.PARTIAL) : FXEvent()

enum class RestartMode {
    PARTIAL,
    FULL
}