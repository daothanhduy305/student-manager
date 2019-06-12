package com.ebolo.studentmanager.repositories;

import com.ebolo.studentmanager.ebolo.database.repositories.mongo.EboloBaseUserMongoRepository;
import com.ebolo.studentmanager.entities.SMUserEntity;

import java.util.List;

/**
 * Repository serves as db entrance to app user info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMUserRepository extends EboloBaseUserMongoRepository<SMUserEntity> {
    /**
     * Method to get a list of users by ids list
     *
     * @param idList list of the users' id
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    List<SMUserEntity> findAllByIdInAndDisabledFalse(final List<String> idList);
}
