package com.ebolo.studentmanager.services

import com.ebolo.common.utils.reflect.copyProperties
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.repositories.SMClassRepository
import org.springframework.stereotype.Service
import tornadofx.*
import java.time.ZoneOffset
import javax.annotation.PostConstruct

@Service
class SMClassService(
    private val classRepository: SMClassRepository
) : Controller() {
    @PostConstruct
    fun setupSubscriptions() {
        // register the student list refresh request and event
        subscribe<SMClassRefreshRequest> {
            fire(SMClassRefreshEvent(getClassList()))
        }
    }

    fun getClassList() = classRepository.findAll().map { classEntity ->
        classEntity.copyProperties(
            destination = SMClassModel.SMClassDto(),
            preProcessedValues = mapOf(
                "startDate" to classEntity.startDate?.atOffset(ZoneOffset.UTC)?.toLocalDate()
            )
        )
    }
}

/**
 * Request to refresh the class list when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
object SMClassRefreshRequest : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the class list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMClassRefreshEvent(val classes: List<SMClassModel.SMClassDto>) : FXEvent()