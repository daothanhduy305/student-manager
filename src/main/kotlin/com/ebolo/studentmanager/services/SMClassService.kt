package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.ebolo.utils.copyProperties
import com.ebolo.studentmanager.ebolo.utils.getWhenPresentOr
import com.ebolo.studentmanager.ebolo.utils.loggerFor
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
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class SMClassService(
    private val classRepository: SMClassRepository,
    private val studentRepository: SMStudentRepository,
    private val feePaidRepository: SMFeePaidRepository,
    private val attendanceRepository: SMAttendanceRepository
) : Controller() {
    private val logger = loggerFor(SMClassService::class.java)

    private var smClassListRefreshRequestRegistration by singleAssign<FXEventRegistration>()
    private var smClassListForStudentRefreshRequestRegistration by singleAssign<FXEventRegistration>()

    @PostConstruct
    private fun setupSubscriptions() {
        // register the class list refresh request and event
        smClassListRefreshRequestRegistration = subscribe<SMClassListRefreshRequest> { request ->
            fire(SMClassListRefreshEvent(getClassList(), request.source))
        }

        // register to the class list refresh request for a specific student
        smClassListForStudentRefreshRequestRegistration = subscribe<SMClassListForStudentRefreshRequest> { request ->
            fire(SMClassListForStudentRefreshEvent(getClassListOfStudent(request.studentId)))
        }
    }

    /**
     * Method to shut down this service
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down class service")
        smClassListRefreshRequestRegistration.unsubscribe()
        smClassListForStudentRefreshRequestRegistration.unsubscribe()
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
     * Method to return a list of all classes that this student has been registered to
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @param studentId String
     * @return List<SMClassDto>
     */
    fun getClassListOfStudent(studentId: String) = classRepository
        .findAllByStudentListContains(studentId)
        .map { it.toDto() }

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
     * Method to delete classes by ids
     *
     * @author saika
     * @since 0.0.1-SNAPSHOT
     *
     * @param classIds List<String>
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun deleteClasses(classIds: List<String>): SMCRUDUtils.SMCRUDResult = try {
        logger.info("Deleting Class(es) '${classIds.joinToString()}'")
        attendanceRepository.deleteAllByClassIdIn(classIds)
        feePaidRepository.deleteAllByClassIdIn(classIds)
        classRepository.deleteAllByIdIn(classIds)

        SMCRUDUtils.SMCRUDResult(true)
    } catch (e: Exception) {
        logger.error(e.message, e)
        SMCRUDUtils.SMCRUDResult(
            false, e.message ?: "Something went wrong while deleting Class(es) '${classIds.joinToString()}'")
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
                        fire(SMClassListRefreshRequest())
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
                fire(SMClassListRefreshRequest())

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
    fun SMClassModel.SMClassDto.getAttendanceInfoList(forDate: LocalDate? = null): List<SMAttendanceEntity> =
        if (forDate == null)
            attendanceRepository.findAllByClassId(this.id)
        else
            attendanceRepository.findAllByClassIdAndYearAndMonthAndDay(
                this.id, forDate.year, forDate.month, forDate.dayOfMonth)

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
     * Method to add an entry into db to mark a fee payment made by a student to a class
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @receiver SMClassModel.SMClassDto
     * @param studentId String
     * @param forDate LocalDate
     * @param paidDate LocalDate?
     * @return SMCRUDUtils.SMCRUDResult
     */
    fun SMClassModel.SMClassDto.updateFeePaidDate(
        studentId: String,
        forDate: LocalDate,
        paidDate: LocalDate? = null
    ): SMCRUDUtils.SMCRUDResult = feePaidRepository
        .findByClassIdAndStudentIdAndYearAndMonth(this.id, studentId, forDate.year, forDate.month)
        .getWhenPresentOr(
            ifPresentHandler = { feePaidEntity ->
                feePaidRepository.save(feePaidEntity.apply {
                    this.paidDate = paidDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
                })
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
class SMClassListRefreshRequest(val source: String = "") : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the class list when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMClassListRefreshEvent(val classes: List<SMClassModel.SMClassDto>, val source: String = "") : FXEvent()

/**
 * Event to refresh a class info
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMClassRefreshEvent(val classDto: SMClassModel.SMClassDto) : FXEvent()

/**
 * Request to refresh the class list for a specific student when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMClassListForStudentRefreshRequest(val studentId: String) : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Event to refresh the class list for a specific student when received
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMClassListForStudentRefreshEvent(val classes: List<SMClassModel.SMClassDto>) : FXEvent()

/**
 * Request to refresh the attendance list for a specific class when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMAttendanceListRefreshRequest(val classId: String) : FXEvent(EventBus.RunOn.BackgroundThread)