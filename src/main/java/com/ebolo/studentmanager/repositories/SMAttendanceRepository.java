package com.ebolo.studentmanager.repositories;

import com.ebolo.studentmanager.ebolo.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMAttendanceEntity;

import java.time.Month;
import java.util.List;
import java.util.Optional;

/**
 * Repository serves as db entrance to attendance info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMAttendanceRepository extends EboloBaseMongoRepository<SMAttendanceEntity> {
    /**
     * Method to get a specific entry for absence info for a class with specific student on specific date
     *
     * @param classId   id of the class
     * @param studentId id of the student
     * @param year      year of the absence day
     * @param month     month of the absence day
     * @param day       day of month of the absence day
     * @return an optional might contain the absence info if there is any
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    Optional<SMAttendanceEntity> findByClassIdAndStudentIdAndYearAndMonthAndDayAndDisabledFalse(
            final String classId,
            final String studentId,
            final int year,
            final Month month,
            final int day
    );

    /**
     * Method to get a list of absence info for a class on a day
     *
     * @param classId id of the class
     * @param year    year of the absence day
     * @param month   month of the absence day
     * @param day     day of month of the absence day
     * @return list of absence info for this class on this day
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    List<SMAttendanceEntity> findAllByClassIdAndYearAndMonthAndDayAndDisabledFalse(
            final String classId,
            final int year,
            final Month month,
            final int day
    );

    /**
     * Method to get a list of absence info in for a class
     *
     * @param classId id of the class to get the info
     * @return list of absence info for this class
     */
    List<SMAttendanceEntity> findAllByClassIdInAndDisabledFalse(final List<String> classId);
}