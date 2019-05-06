package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseUserMongoRepository;
import com.ebolo.studentmanager.entities.SMStudentEntity;

import java.util.List;
import java.util.Set;

/**
 * Repository serves as db entrance to student info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMStudentRepository extends EboloBaseUserMongoRepository<SMStudentEntity> {
    /**
     * Method to retrieve a list of the students having the id in a reference list
     *
     * @param studentIdList list of id used as the reference
     * @return list of student that are found
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    List<SMStudentEntity> findAllByIdIn(Set<String> studentIdList);

    /**
     * Method to delete a list of students that have the Id being contained
     *
     * @param studentIdList list of students' id being deleted
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    void deleteAllByIdIn(final List<String> studentIdList);
}
