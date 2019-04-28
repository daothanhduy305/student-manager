package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMSubjectEntity;

import java.util.Optional;

/**
 * Repository serves as db entrance to subject info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
public interface SMSubjectRepository extends EboloBaseMongoRepository<SMSubjectEntity> {
    /**
     * Method to retrieve a subject by its name
     *
     * @param subjectName name of the subject to search
     * @return an optional that might contain the subject if there is any
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    Optional<SMSubjectEntity> getByName(final String subjectName);
}
