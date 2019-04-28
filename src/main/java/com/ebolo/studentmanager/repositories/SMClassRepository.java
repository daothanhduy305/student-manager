package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMClassEntity;

/**
 * Repository serves as db entrance to class info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMClassRepository extends EboloBaseMongoRepository<SMClassEntity> {
}
