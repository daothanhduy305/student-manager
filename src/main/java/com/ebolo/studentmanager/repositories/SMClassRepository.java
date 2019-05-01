package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMClassEntity;

import java.util.List;

/**
 * Repository serves as db entrance to class info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMClassRepository extends EboloBaseMongoRepository<SMClassEntity> {
    List<SMClassEntity> findAllByStudentListContains(final String studentId);
}
