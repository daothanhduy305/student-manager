package com.ebolo.studentmanager.services

import com.ebolo.common.utils.getWhenPresentOr
import com.ebolo.common.utils.loggerFor
import com.ebolo.common.utils.reflect.copyProperties
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.repositories.SMClassRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.stereotype.Service
import tornadofx.Controller
import tornadofx.EventBus
import tornadofx.FXEvent
import java.time.ZoneOffset
import javax.annotation.PostConstruct

@Service
class SMClassService(
    private val classRepository: SMClassRepository
) : Controller() {
    val logger = loggerFor(SMClassService::class.java)

    @PostConstruct
    fun setupSubscriptions() {
        // register the student list refresh request and event
        subscribe<SMClassRefreshRequest> {
            fire(SMClassRefreshEvent(getClassList()))
        }
    }

    /**
     * Method to get the list of all classes available
     *
     * @author saika
     * @since 0.0.1-SNAPSHOT
     *
     * @return List<SMClassDto>
     */
    fun getClassList() = classRepository.findAll().map { classEntity ->
        classEntity.copyProperties(
            destination = SMClassModel.SMClassDto(),
            preProcessedValues = mapOf(
                "startDate" to classEntity.startDate?.atOffset(ZoneOffset.UTC)?.toLocalDate()
            )
        )
    }

    /**
     * Method to create a new class object
     *
     * @author saika
     * @since 0.0.1-SNAPSHOT
     *
     * @param classModel SMClassModel
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun createNewClass(classModel: SMClassModel): SMCRUDUtils.SMCRUDResult {
        classRepository.save(classModel.getEntity())

        return SMCRUDUtils.SMCRUDResult(true)
    }

    /**
     * Method to delete a class by its id
     *
     * @author saika
     * @since 0.0.1-SNAPSHOT
     *
     * @param classId String
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun deleteClass(classId: String): SMCRUDUtils.SMCRUDResult = try {
        logger.info("Deleting Class '$classId'")
        classRepository.deleteById(classId)

        SMCRUDUtils.SMCRUDResult(true)
    } catch (e: Exception) {
        logger.error(e.message, e)
        SMCRUDUtils.SMCRUDResult(false, e.message ?: "Something went wrong while deleting Class '$classId'")
    }

    /**
     * Method to edit a class object's detail
     *
     * @author saika
     * @since 0.0.1-SNAPSHOT
     *
     * @param classModel SMClassModel
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun editClass(classModel: SMClassModel): SMCRUDUtils.SMCRUDResult = classRepository.findById(classModel.id.value)
        .getWhenPresentOr(
            ifPresentHandler = {
                classRepository.save(classModel.getEntity())
                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false, "Class '${classModel.id.value}' could not be found")
            }
        )
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