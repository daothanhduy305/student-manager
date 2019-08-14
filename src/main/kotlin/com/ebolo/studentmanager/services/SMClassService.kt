package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.StudentManagerApplication
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
import java.util.*
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

    @PostConstruct
    private fun setupSubscriptions() {
        // register the class list refresh request and event
        smClassListRefreshRequestRegistration = subscribe<SMClassListRefreshRequest> { request ->
            runLater {
                StudentManagerApplication.startSync()

                runAsync {
                    val classList = getClassList()
                    fire(SMClassListRefreshEvent(classList, request.source))
                }
            }
        }
    }

    /**
     * Method to shut down this service
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down class service")
        smClassListRefreshRequestRegistration.unsubscribe()
    }

    /**
     * Method to get the list of all classes available
     *
     * @author saika
     * @since 0.0.1-SNAPSHOT
     *
     * @return List<SMClassDto>
     */
    fun getClassList() = classRepository.findAllByDisabledFalse().map { it.toDto() }

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
        .findAllByStudentListContainsAndDisabledFalse(studentId)
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
        attendanceRepository.saveAll(attendanceRepository.findAllByClassIdInAndDisabledFalse(classIds).map {
            it.apply { disabled = true }
        })
        feePaidRepository.saveAll(feePaidRepository.findAllByClassIdInAndDisabledFalse(classIds).map {
            it.apply { it.disabled = true }
        })
        classRepository.saveAll(classRepository.findAllByIdInAndDisabledFalse(classIds).map {
            it.apply { disabled = true }
        })

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
                            fire(SMClassListRefreshRequest())
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
        .findByClassIdAndStudentIdAndYearAndMonthAndDayAndDisabledFalse(this.id, studentId, forDate.year, forDate.month, forDate.dayOfMonth)
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
            attendanceRepository.findAllByClassIdInAndDisabledFalse(listOf(this.id))
        else
            attendanceRepository.findAllByClassIdAndYearAndMonthAndDayAndDisabledFalse(
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
        .findByClassIdAndStudentIdAndYearAndMonthAndDisabledFalse(this.id, studentId, forDate.year, forDate.month)
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
     * Method to update the paid date for a fee payment entry
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
        .findByClassIdAndStudentIdAndYearAndMonthAndDisabledFalse(this.id, studentId, forDate.year, forDate.month)
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
     * Method to update the note for a fee payment entry
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
    fun SMClassModel.SMClassDto.updateFeePaidNote(
        studentId: String,
        forDate: LocalDate,
        note: String = ""
    ): SMCRUDUtils.SMCRUDResult = feePaidRepository
        .findByClassIdAndStudentIdAndYearAndMonthAndDisabledFalse(this.id, studentId, forDate.year, forDate.month)
        .getWhenPresentOr(
            ifPresentHandler = { feePaidEntity ->
                feePaidRepository.save(feePaidEntity.apply {
                    this.note = note
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
     * @return List<SMFeePaidEntity>
     */
    fun SMClassModel.SMClassDto.getTuitionFeePaymentInfo(): List<SMFeePaidEntity> = feePaidRepository
        .findAllByClassIdInAndDisabledFalse(listOf(this.id))

    /**
     * Method to retrieve the info the tuition fee paid for a specific student
     *
     * @author ebolo
     * @since 1.0.3
     *
     * @receiver SMClassModel.SMClassDto
     * @param studentId String
     */
    fun SMClassModel.SMClassDto.getTuitionFeePaymentInfo(studentId: String): List<Pair<LocalDate, Optional<SMFeePaidEntity>>> {
        val madePayments = feePaidRepository.findAllByClassIdAndStudentId(this.id, studentId)
        val paymentList = mutableListOf<Pair<LocalDate, Optional<SMFeePaidEntity>>>()
        var currentDate = this.startDate

        (1..this.monthPeriods.toInt()).forEach { _ ->
            val paymentMadeForThisMonth = madePayments
                .firstOrNull { payment -> payment.year == currentDate.year && payment.month == currentDate.month }
            paymentList.add(currentDate to Optional.ofNullable(paymentMadeForThisMonth))
            currentDate = currentDate.plusMonths(1)
        }

        return paymentList
    }
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
 * Request to refresh the attendance list for a specific class when fired
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
class SMAttendanceListRefreshRequest(val classId: String) : FXEvent(EventBus.RunOn.BackgroundThread)


/**
 * Request to be thrown out when a fee payment info has been changed
 *
 * @author ebolo
 * @since 1.0.3
 *
 * @property classId String?
 * @property studentId String?
 * @constructor
 */
class SMFeePaidRefreshRequest(val classId: String?, val studentId: String?) : FXEvent(EventBus.RunOn.BackgroundThread)