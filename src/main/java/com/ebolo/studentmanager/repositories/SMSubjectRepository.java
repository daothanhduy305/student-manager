package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseMongoRepository;
import com.ebolo.studentmanager.entities.SMSubjectEntity;

import java.util.Optional;

public interface SMSubjectRepository extends EboloBaseMongoRepository<SMSubjectEntity> {
    Optional<SMSubjectEntity> getByName(final String subjectName);
}
