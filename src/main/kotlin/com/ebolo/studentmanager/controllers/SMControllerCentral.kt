package com.ebolo.studentmanager.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

/**
 * This class serve as a central for all the available controllers within the system
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property userController SMUserController
 * @constructor
 */
@Controller
class SMControllerCentral(
    @Autowired val userController: SMUserController
)