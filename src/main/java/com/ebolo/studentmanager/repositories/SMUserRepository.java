package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseUserMongoRepository;
import com.ebolo.studentmanager.entities.SMUserEntity;

import java.util.List;

/**
 * Repository serves as db entrance to app user info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMUserRepository extends EboloBaseUserMongoRepository<SMUserEntity> {
    void deleteAllByIdIn(final List<String> idList);
}
