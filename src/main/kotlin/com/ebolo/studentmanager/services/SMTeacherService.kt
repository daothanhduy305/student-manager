package com.ebolo.studentmanager.services

import com.ebolo.common.utils.getWhenPresentOr
import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.models.SMTeacherModel
import com.ebolo.studentmanager.repositories.SMTeacherRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.stereotype.Service
import tornadofx.*
import javax.annotation.PostConstruct

/**
 * Service class contains functionality upon teacher
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property teacherRepository SMTeacherRepository
 * @constructor
 */
@Service
class SMTeacherService(
    private val teacherRepository: SMTeacherRepository
) : Controller() {
    private val logger = loggerFor(SMTeacherService::class.java)

    @PostConstruct
    fun setupSubscriptions() {
        // register the student list refresh request and event
        subscribe<SMTeacherRefreshRequest> {
            fire(SMTeacherRefreshEvent(getTeacherList()))
        }
    }

    /**
     * Method to retrieve the teach list available in the system
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @return List<SMTeacherModel.SMTeacherDto>
     */
    fun getTeacherList(): List<SMTeacherModel.SMTeacherDto> = teacherRepository
        .findAll()
        .map { it.toDto() }

    /**
     * Method to create new teacher
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param teacherModel SMTeacherModel
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun createNewTeacher(teacherModel: SMTeacherModel): SMCRUDUtils.SMCRUDResult {
        teacherRepository.save(teacherModel.getEntity())

        return SMCRUDUtils.SMCRUDResult(true)
    }

    /**
     * Method to modify the teacher information
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param teacherModel SMTeacherModel
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun editTeacher(teacherModel: SMTeacherModel): SMCRUDUtils.SMCRUDResult = teacherRepository
        .findById(teacherModel.id.value)
        .getWhenPresentOr(
            ifPresentHandler = {
                teacherRepository.save(teacherModel.getEntity())
                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false, "Không tìm thấy dữ liệu giáo viên")
            }
        )

    /**
     * Method to delete a list of teachers from the system
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param idList List<String>
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun deleteTeachers(idList: List<String>): SMCRUDUtils.SMCRUDResult = try {
        logger.info("Deleting Teachers(s) '${idList.joinToString()}'")
        teacherRepository.deleteAllByIdIn(idList)

        SMCRUDUtils.SMCRUDResult(true)
    } catch (e: Exception) {
        SMCRUDUtils.SMCRUDResult(false, e.message ?: "Something went wrong")
    }
}

/**
 * Request to refresh the teacher list when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
object SMTeacherRefreshRequest : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the teacher list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMTeacherRefreshEvent(val teachers: List<SMTeacherModel.SMTeacherDto>) : FXEvent()