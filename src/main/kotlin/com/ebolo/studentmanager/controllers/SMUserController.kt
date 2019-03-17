package com.ebolo.studentmanager.controllers

import com.ebolo.studentmanager.entities.SMUserEntity
import com.ebolo.studentmanager.repositories.SMUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

@Controller
class SMUserController(
    @Autowired private val userRepository: SMUserRepository
) {
    fun login(user: SMUserEntity): Boolean {
        return true
    }

    fun validateLoginUserInfo(user: SMUserEntity): Boolean {
        return true
    }
}