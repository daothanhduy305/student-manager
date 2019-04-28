package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseUserMongoRepository;
import com.ebolo.studentmanager.entities.SMTeacherEntity;

/**
 * Repository serves as db entrance to teacher info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMTeacherRepository extends EboloBaseUserMongoRepository<SMTeacherEntity> {
}
