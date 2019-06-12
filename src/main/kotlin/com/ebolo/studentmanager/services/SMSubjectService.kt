package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.repositories.SMSubjectRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.stereotype.Service
import tornadofx.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

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

    private var smSubjectRefreshRequestRegistration by singleAssign<FXEventRegistration>()

    @PostConstruct
    fun setupSubscriptions() {
        // register the subject list refresh request and event
        smSubjectRefreshRequestRegistration = subscribe<SMSubjectRefreshRequest> { request ->
            fire(SMSubjectRefreshEvent(getSubjects(), request.source))
        }
    }

    /**
     * Method to shut down this service
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down subject service")
        smSubjectRefreshRequestRegistration.unsubscribe()
    }

    /**
     * Method to retrieve all the available subject in database and transform it to the model
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @return List<SMSubjectModel>
     */
    fun getSubjects(): List<SMSubjectModel.SMSubjectDto> = subjectRepository.findAllByDisabledFalse().map { it.toDto() }

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
        val added = subjectRepository.findByNameIgnoreCaseAndDisabledFalse(subjectModel.name.value).isPresent

        if (!added) {
            subjectRepository.save(subjectModel.getEntity())
        }

        fire(SMDataProcessRequest {
            fire(SMSubjectRefreshRequest())
        })

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

        subjectRepository.saveAll(subjectRepository.findAllByIdInAndDisabledFalse(idList).map {
            it.apply { disabled = true }
        })
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
class SMSubjectRefreshRequest(val source: String = "") : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the student list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMSubjectRefreshEvent(val subjects: List<SMSubjectModel.SMSubjectDto>, val source: String = "") : FXEvent()