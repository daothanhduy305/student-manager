package com.ebolo.studentmanager.repositories;

import com.ebolo.studentmanager.ebolo.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMSubjectEntity;

import java.util.List;
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
    Optional<SMSubjectEntity> findByNameIgnoreCase(final String subjectName);

    /**
     * Method to delete a list of subjects that have the Id being contained
     *
     * @param subjectIdList list of subjects' id being deleted
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    void deleteAllByIdIn(final List<String> subjectIdList);
}
