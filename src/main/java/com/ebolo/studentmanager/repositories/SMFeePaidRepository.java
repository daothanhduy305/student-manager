package com.ebolo.studentmanager.repositories;

import com.ebolo.studentmanager.ebolo.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMFeePaidEntity;

import java.time.Month;
import java.util.List;
import java.util.Optional;

/**
 * Repository serves as db entrance to tuition fee payment info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMFeePaidRepository extends EboloBaseMongoRepository<SMFeePaidEntity> {
    /**
     * Method to retrieve tuition fee payment for specific info provided
     *
     * @param classId   id of the class
     * @param studentId id of the student
     * @param year      year of the payment made
     * @param month     month of the payment made
     * @return optional that might contain the payment info if there is any
     */
    Optional<SMFeePaidEntity> findByClassIdAndStudentIdAndYearAndMonthAndDisabledFalse(
            final String classId,
            final String studentId,
            final int year,
            final Month month
    );

    /**
     * Method to retrieve the list of payment made for this class in this month
     *
     * @param classId d of the class
     * @param year    year of the payments made
     * @param month   month of the payments made
     * @return list of the payments made
     */
    List<SMFeePaidEntity> findAllByClassIdAndYearAndMonthAndDisabledFalse(
            final String classId,
            final int year,
            final Month month
    );

    /**
     * Method to get all the fee payment info having the mentioned class id
     *
     * @param classIds list of id of the classes to delete the fee payment info
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    List<SMFeePaidEntity> findAllByClassIdInAndDisabledFalse(final List<String> classIds);

    /**
     * Method to retrieve all the payment info made by a student for a class
     *
     * @param classId   id of the class that this student is in
     * @param studentId id of the student in request
     * @return list of payment made
     * @author ebolo
     * @since 1.0.3
     */
    List<SMFeePaidEntity> findAllByClassIdAndStudentId(final String classId, final String studentId);
}
