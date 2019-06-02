package com.ebolo.studentmanager.services

import org.springframework.stereotype.Service

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
)