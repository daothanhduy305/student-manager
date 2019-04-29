package com.ebolo.studentmanager.services

import com.ebolo.common.utils.getWhenPresentOr
import com.ebolo.common.utils.loggerFor
import com.ebolo.common.utils.reflect.copyProperties
import com.ebolo.studentmanager.entities.SMAttendanceEntity
import com.ebolo.studentmanager.entities.SMFeePaidEntity
import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import com.ebolo.studentmanager.models.SMClassModel
import com.ebolo.studentmanager.models.SMStudentModel
import com.ebolo.studentmanager.repositories.SMAttendanceRepository
import com.ebolo.studentmanager.repositories.SMClassRepository
import com.ebolo.studentmanager.repositories.SMFeePaidRepository
import com.ebolo.studentmanager.repositories.SMStudentRepository
import com.ebolo.studentmanager.utils.SMCRUDUtils
import org.springframework.stereotype.Service
import tornadofx.*
import java.time.LocalDate
import javax.annotation.PostConstruct

@Service
class SMClassService(
    private val classRepository: SMClassRepository,
    private val studentRepository: SMStudentRepository,
    private val feePaidRepository: SMFeePaidRepository,
    private val attendanceRepository: SMAttendanceRepository
) : Controller() {
    val logger = loggerFor(SMClassService::class.java)

    @PostConstruct
    private fun setupSubscriptions() {
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

                            this.studentPerformanceList.removeIf {
                                it.student == this@deregisterFromClass.id
                            }
                        })

                        fire(SMClassRefreshEvent(classEntity.toDto()))
                        fire(SMClassListRefreshRequest)
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
     * Method to be used to update the performance info of a student in a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMStudentModel.SMStudentDto
     * @param classId String
     * @param performanceInfo SMStudentPerformanceInfo
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun SMStudentModel.SMStudentDto.updatePerformanceInfo(
        classId: String, performanceInfo: SMStudentPerformanceInfo
    ): SMCRUDUtils.SMCRUDResult = classRepository.findById(classId)
        .getWhenPresentOr(
            ifPresentHandler = { classEntity ->
                val performanceInfoIndex = classEntity.studentPerformanceList.indexOfFirst { it.student == this.id }

                if (performanceInfoIndex == -1) {
                    classEntity.studentPerformanceList.add(performanceInfo)
                } else {
                    performanceInfo.copyProperties(classEntity.studentPerformanceList[performanceInfoIndex])
                }

                classRepository.save(classEntity)

                fire(SMClassRefreshEvent(classEntity.toDto()))
                fire(SMClassListRefreshRequest)

                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false, "Không tìm thấy lớp")
            }
        )

    /**
     * Method to add an absence info of a student into a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMClassModel.SMClassDto
     * @param studentId String
     * @param forDate LocalDate
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun SMClassModel.SMClassDto.addAbsenceInfo(studentId: String, forDate: LocalDate): SMCRUDUtils.SMCRUDResult {
        attendanceRepository.save(
            SMAttendanceEntity(studentId, this.id, forDate.year, forDate.month, forDate.dayOfMonth)
        )

        return SMCRUDUtils.SMCRUDResult(true)
    }

    /**
     * Method to remove an absence info of a student into a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMClassModel.SMClassDto
     * @param studentId String
     * @param forDate LocalDate
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun SMClassModel.SMClassDto.deleteAbsenceInfo(
        studentId: String, forDate: LocalDate
    ): SMCRUDUtils.SMCRUDResult = attendanceRepository
        .findByClassIdAndStudentIdAndYearAndMonthAndDay(this.id, studentId, forDate.year, forDate.month, forDate.dayOfMonth)
        .getWhenPresentOr(
            ifPresentHandler = {
                attendanceRepository.delete(it)
                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false)
            }
        )

    /**
     * Method to retrieve the list of all attendance info available for this class and on this specific date
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMClassModel.SMClassDto
     * @param forDate LocalDate
     * @return List<SMAttendanceEntity>
     */
    fun SMClassModel.SMClassDto.getAttendanceInfoList(forDate: LocalDate): List<SMAttendanceEntity> = attendanceRepository
        .findAllByClassIdAndYearAndMonthAndDay(this.id, forDate.year, forDate.month, forDate.dayOfMonth)

    /**
     * Method to add an entry into db to mark a fee payment made by a student to a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMClassModel.SMClassDto
     * @param studentId String
     * @param forDate LocalDate
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun SMClassModel.SMClassDto.addFeePaidInfo(studentId: String, forDate: LocalDate): SMCRUDUtils.SMCRUDResult {
        feePaidRepository.save(
            SMFeePaidEntity(studentId, this.id, forDate.year, forDate.month)
        )

        return SMCRUDUtils.SMCRUDResult(true)
    }

    /**
     * Method to remove an entry into db to mark a fee payment made by a student to a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMClassModel.SMClassDto
     * @param studentId String
     * @param forDate LocalDate
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun SMClassModel.SMClassDto.deleteFeePaidInfo(
        studentId: String, forDate: LocalDate
    ): SMCRUDUtils.SMCRUDResult = feePaidRepository
        .findByClassIdAndStudentIdAndYearAndMonth(this.id, studentId, forDate.year, forDate.month)
        .getWhenPresentOr(
            ifPresentHandler = {
                feePaidRepository.delete(it)
                SMCRUDUtils.SMCRUDResult(true)
            },
            otherwise = {
                SMCRUDUtils.SMCRUDResult(false)
            }
        )

    /**
     * Method to retrieve the list of all payment info available for this class and this specific month
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMClassModel.SMClassDto
     * @param forDate LocalDate
     * @return List<SMFeePaidEntity>
     */
    fun SMClassModel.SMClassDto.getTuitionFeePaymentInfo(forDate: LocalDate): List<SMFeePaidEntity> = feePaidRepository
        .findAllByClassIdAndYearAndMonth(this.id, forDate.year, forDate.month)
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