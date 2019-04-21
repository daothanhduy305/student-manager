package com.ebolo.studentmanager.services

import com.ebolo.common.utils.getWhenPresentOr
import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.repositories.SMClassRepository
import com.ebolo.studentmanager.repositories.SMStudentRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.stereotype.Service
import tornadofx.*
import javax.annotation.PostConstruct

@Service
class SMClassService(
    private val classRepository: SMClassRepository,
    private val studentRepository: SMStudentRepository
) : Controller() {
    val logger = loggerFor(SMClassService::class.java)

    @PostConstruct
    fun setupSubscriptions() {
        // register the student list refresh request and event
        subscribe<SMClassListRefreshRequest> {
            fire(SMClassListRefreshEvent(getClassList()))
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
    fun getClassList() = classRepository.findAll().map { it.toDto() }

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

    /**
     * Method to register a student into a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMStudentModel.SMStudentDto
     * @param classModel SMClassModel
     */
    infix fun SMStudentModel.SMStudentDto.registerToClass(classModel: SMClassModel): SMCRUDUtils.SMCRUDResult = classRepository
        .findById(classModel.item.id)
        .getWhenPresentOr(
            ifPresentHandler = { classEntity ->
                studentRepository.findById(this.id).getWhenPresentOr(
                    ifPresentHandler = { studentEntity ->
                        if (classEntity.studentList.any { student -> student.id == this@registerToClass.id })
                            SMCRUDUtils.SMCRUDResult(false, "Học viên đã được đăng kí vào lớp này")
                        else {
                            classRepository.save(classEntity.apply {
                                this.studentList.add(studentEntity)
                            })

                            fire(SMClassRefreshEvent(classEntity.toDto()))
                            SMCRUDUtils.SMCRUDResult(true)
                        }
                    },
                    otherwise = {
                        SMCRUDUtils.SMCRUDResult(false, "Học viên không tồn tại trong hệ thống")
                    }
                )
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false, "Lớp  học không tồn tại trong hệ thống")
            }
        )

    /**
     * Method to deregister a student from a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMStudentModel.SMStudentDto
     * @param classModel SMClassModel
     */
    infix fun SMStudentModel.SMStudentDto.deregisterFromClass(classModel: SMClassModel): SMCRUDUtils.SMCRUDResult = classRepository
        .findById(classModel.item.id)
        .getWhenPresentOr(
            ifPresentHandler = { classEntity ->
                studentRepository.findById(this.id).getWhenPresentOr(
                    ifPresentHandler = {
                        classRepository.save(classEntity.apply {
                            this.studentList.removeIf {
                                it.id == this@deregisterFromClass.id
                            }
                        })

                        fire(SMClassRefreshEvent(classEntity.toDto()))
                        SMCRUDUtils.SMCRUDResult(true)
                    },
                    otherwise = {
                        SMCRUDUtils.SMCRUDResult(false, "Học viên không tồn tại trong hệ thống")
                    }
                )
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false, "Lớp  học không tồn tại trong hệ thống")
            }
        )

    /**
     * Method to update a performance result for a student in a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMStudentModel.SMStudentDto
     * @param classId String
     * @param resultIndex Int The index of the result to be update
     * @param newResult Int new value for the result
     */
    fun SMStudentModel.SMStudentDto.updateResult(
        classId: String, resultIndex: Int, newResult: Int
    ): SMCRUDUtils.SMCRUDResult = classRepository.findById(classId)
        .getWhenPresentOr(
            ifPresentHandler = { classEntity ->
                val performanceInfoIndex = classEntity.studentPerformanceList.indexOfFirst { it.student == this.id }

                if (performanceInfoIndex == -1) {
                    classEntity.studentPerformanceList.add(SMStudentPerformanceInfo(
                        student = this.id,
                        note = "",
                        results = generateSequence { -1 }.take(classEntity.numberOfExams).toMutableList().apply {
                            this[resultIndex] = newResult
                        }
                    ))
                } else {
                    val newInfo = classEntity.studentPerformanceList[performanceInfoIndex].apply {
                        results[resultIndex] = newResult
                    }

                    classEntity.studentPerformanceList.set(performanceInfoIndex, newInfo)
                }

                classRepository.save(classEntity)

                fire(SMClassRefreshEvent(classEntity.toDto()))

                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false, "Không tìm thấy lớp")
            }
        )
}

/**
 * Request to refresh the class list when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
object SMClassListRefreshRequest : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the class list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMClassListRefreshEvent(val classes: List<SMClassModel.SMClassDto>) : FXEvent()

/**
 * Event to refresh a class info
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMClassRefreshEvent(val classDto: SMClassModel.SMClassDto) : FXEvent()