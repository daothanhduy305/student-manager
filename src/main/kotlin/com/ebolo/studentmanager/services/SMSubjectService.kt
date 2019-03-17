package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.entities.SMSubjectEntity
import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.repositories.SMClassRepository
import com.ebolo.studentmanager.repositories.SMSubjectRepository
import com.ebolo.studentmanager.utils.SMCRUDResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tornadofx.*
import javax.annotation.PostConstruct

@Service
class SMSubjectService(
    @Autowired private val classRepository: SMClassRepository,
    @Autowired private val subjectRepository: SMSubjectRepository
) : Controller() {
    @PostConstruct
    fun setupSubscriptions() {
        subscribe<SMSubjectRefreshRequest> {
            fire(SMSubjectRefreshEvent(getAllAvailableSubjects()))
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
    fun getAllAvailableSubjects() = subjectRepository.findAll().map { subjectEntity ->
        SMSubjectModel.SMSubjectDto().also {
            it.name = subjectEntity.name
        }
    }

    /**
     * Method check if the db has already contained the subject or not and add new if not
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @param subjectName String
     * @return SMCRUDResult
     */
    fun createNewSubject(subjectName: String): SMCRUDResult {
        val added = subjectRepository.getByName(subjectName).isPresent

        if (!added) {
            subjectRepository.save(SMSubjectEntity().also {
                it.name = subjectName
            })
        }

        return SMCRUDResult(
            success = !added,
            errorMessage = if (added) "Môn học đã có trong cơ sở dữ liệu" else ""
        )
    }
}

object SMSubjectRefreshRequest : FXEvent(EventBus.RunOn.BackgroundThread)
class SMSubjectRefreshEvent(val subjects: List<SMSubjectModel.SMSubjectDto>) : FXEvent()