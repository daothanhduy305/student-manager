package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.ebolo.utils.copyProperties
import com.ebolo.studentmanager.ebolo.utils.getWhenPresentOr
import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.repositories.SMClassRepository
import com.ebolo.studentmanager.repositories.SMStudentRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.stereotype.Service
import tornadofx.*
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Service class to provide functionality over students in system
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property studentRepository SMStudentRepository
 * @property classRepository SMClassRepository
 * @constructor
 */
@Service
class SMStudentService(
    private val studentRepository: SMStudentRepository,
    private val classRepository: SMClassRepository
) : Controller() {
    private val logger = loggerFor(SMStudentService::class.java)

    private var smStudentRefreshRequestRegistration by singleAssign<FXEventRegistration>()

    @PostConstruct
    fun setupSubscriptions() {
        // register the student list refresh request and event
        smStudentRefreshRequestRegistration = subscribe<SMStudentRefreshRequest> { request ->
            fire(SMStudentRefreshEvent(getStudentList(), request.source))
        }
    }

    /**
     * Method to shut down this service
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down student service")
        smStudentRefreshRequestRegistration.unsubscribe()
    }

    /**
     * Method to create new student instance base on the info being passed
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.0.1-SNAPSHOT
     *
     * @param studentModel SMStudentModel
     */
    fun createNewStudent(studentModel: SMStudentModel): SMCRUDUtils.SMCRUDResult {
        studentRepository.save(studentModel.getEntity())

        return SMCRUDUtils.SMCRUDResult(
            success = true
        )
    }

    /**
     * Method to retrieve the list of student in dto form
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @return List<SMStudentModel.SMStudentDto>
     */
    fun getStudentList(): List<SMStudentModel.SMStudentDto> = studentRepository
        .findAllByDisabledFalse()
        .map { studentEntity ->
            studentEntity.copyProperties(
                destination = SMStudentModel.SMStudentDto(),
                preProcessedValues = mapOf(
                    // pre-process the birthday since we must use LocalDate for the model - for datepicker
                    "birthday" to (
                        if (studentEntity.birthday != null)
                            studentEntity.birthday!!.atOffset(ZoneOffset.UTC).toLocalDate()
                        else
                            null
                        )
                )
            )
        }

    /**
     * Method to delete a list of students by ids
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param idList List<String>
     * @return SMCRUDUtils
     */
    fun deleteStudents(idList: List<String>): SMCRUDUtils.SMCRUDResult = try {
        logger.info("Deleting Student(s) '${idList.joinToString()}'")
        studentRepository.saveAll(studentRepository.findAllByIdInAndDisabledFalse(idList).map {
            it.apply { disabled = true }
        })
        SMCRUDUtils.SMCRUDResult(true)
    } catch (e: Exception) {
        SMCRUDUtils.SMCRUDResult(false, errorMessage = e.message ?: "Something went wrong")
    }

    /**
     * Method to edit the student data base on the passing information from the dto
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param studentModel SMStudentModel the editing student information
     * @return SMCRUDUtils
     */
    fun editStudent(studentModel: SMStudentModel): SMCRUDUtils.SMCRUDResult = try {
        studentRepository.findById(studentModel.id.value).getWhenPresentOr(
            ifPresentHandler = {
                studentRepository.save(studentModel.getEntity())
                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = { SMCRUDUtils.SMCRUDResult(false, "Không tìm thấy dữ liệu học sinh.") }
        )
    } catch (e: Exception) {
        SMCRUDUtils.SMCRUDResult(false, e.message ?: "")
    }
}

/**
 * Request to refresh the student list when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMStudentRefreshRequest(val source: String = "") : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the student list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMStudentRefreshEvent(val students: List<SMStudentModel.SMStudentDto>, val source: String = "") : FXEvent()