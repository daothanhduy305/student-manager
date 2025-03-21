package com.ebolo.studentmanager.repositories;

import com.ebolo.studentmanager.ebolo.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMClassEntity;

import java.util.List;

/**
 * Repository serves as db entrance to class info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMClassRepository extends EboloBaseMongoRepository<SMClassEntity> {
    /**
     * Method to retrieve list of students by Id
     *
     * @param studentId list of student's Id
     * @return list of SMStudentEntity
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    List<SMClassEntity> findAllByStudentListContainsAndDisabledFalse(final String studentId);

    /**
     * Get a list of students by ids
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @param classIds list of student's id to be deleted
     */
    List<SMClassEntity> findAllByIdInAndDisabledFalse(final List<String> classIds);
}
