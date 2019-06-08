package com.ebolo.studentmanager.repositories;

import com.ebolo.studentmanager.ebolo.database.repositories.mongo.EboloBaseUserMongoRepository;
import com.ebolo.studentmanager.entities.SMTeacherEntity;

import java.util.List;

/**
 * Repository serves as db entrance to teacher info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMTeacherRepository extends EboloBaseUserMongoRepository<SMTeacherEntity> {
    /**
     * Method to delete a list of teachers that have the Id being contained
     *
     * @param teacherIdList list of teachers' id being deleted
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    void deleteAllByIdIn(final List<String> teacherIdList);
}
