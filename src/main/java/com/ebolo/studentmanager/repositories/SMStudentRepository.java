package com.ebolo.studentmanager.repositories;

import com.ebolo.studentmanager.ebolo.database.repositories.mongo.EboloBaseUserMongoRepository;
import com.ebolo.studentmanager.entities.SMStudentEntity;

import java.util.List;

/**
 * Repository serves as db entrance to student info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMStudentRepository extends EboloBaseUserMongoRepository<SMStudentEntity> {
    /**
     * Method to get a list of students that have the Id being contained
     *
     * @param studentIdList list of students' id being deleted
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    List<SMStudentEntity> findAllByIdInAndDisabledFalse(final List<String> studentIdList);
}
