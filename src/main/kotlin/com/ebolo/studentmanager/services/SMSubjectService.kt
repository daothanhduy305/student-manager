package com.ebolo.studentmanager.services

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.repositories.SMSubjectRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.stereotype.Service
import tornadofx.*
import javax.annotation.PostConstruct

/**
 * Service class to provide functionality over the subjects
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property subjectRepository SMSubjectRepository
 * @property logger Logger
 * @constructor
 */
@Service
class SMSubjectService(
    private val subjectRepository: SMSubjectRepository
) : Controller() {
    private val logger = loggerFor(SMSubjectService::class.java)

    @PostConstruct
    fun setupSubscriptions() {
        // register the subject list refresh request and event
        subscribe<SMSubjectRefreshRequest> {
            fire(SMSubjectRefreshEvent(getSubjects()))
        }
    }

    /**
     * Method to retrieve all the available subject in database and transform it to the model
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @return List<SMSubjectModel>
     */
    fun getSubjects(): List<SMSubjectModel.SMSubjectDto> = subjectRepository.findAll().map { it.toDto() }

    /**
     * Method check if the db has already contained the subject or not and add new if not
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param subjectModel SMSubjectModel model contains info of the subject
     * @return SMCRUDUtils
     */
    fun createNewOrUpdateSubject(subjectModel: SMSubjectModel): SMCRUDUtils.SMCRUDResult {
        val added = subjectRepository.findByNameIgnoreCase(subjectModel.name.value).isPresent

        if (!added) {
            subjectRepository.save(subjectModel.getEntity())
        }

        fire(SMSubjectRefreshRequest)

        return SMCRUDUtils.SMCRUDResult(
            success = !added,
            errorMessage = if (added) "Môn học đã có trong cơ sở dữ liệu" else ""
        )
    }

    /**
     * Wrap method for internal deleting
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param idList List<String>
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun deleteSubjects(idList: List<String>): SMCRUDUtils.SMCRUDResult = try {
        logger.info("Deleting Subjects(s) '${idList.joinToString()}'")

        subjectRepository.deleteAllByIdIn(idList)
        SMCRUDUtils.SMCRUDResult(true)
    } catch (e: Exception) {
        SMCRUDUtils.SMCRUDResult(false, errorMessage = e.message ?: "Something went wrong")
    }
}

/**
 * Request to refresh the subject list when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
object SMSubjectRefreshRequest : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the student list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMSubjectRefreshEvent(val subjects: List<SMSubjectModel.SMSubjectDto>) : FXEvent()