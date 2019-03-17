package com.ebolo.studentmanager.services

import org.springframework.beans.factory.annotation.Autowired
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
    @Autowired val userService: SMUserService,
    @Autowired val cacheService: SMCacheService,
    @Autowired val subjectService: SMSubjectService
)