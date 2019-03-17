package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.repositories.SMClassRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SMSubjectService(
    @Autowired private val classRepository: SMClassRepository
)