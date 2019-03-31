package com.ebolo.studentmanager.repositories;

import com.ebolo.common.database.repositories.mongo.EboloBaseUserMongoRepository;
import com.ebolo.studentmanager.entities.SMStudentEntity;

import java.util.List;
import java.util.Set;

public interface SMStudentRepository extends EboloBaseUserMongoRepository<SMStudentEntity> {
    List<SMStudentEntity> findAllByIdIn(Set<String> studentIdList);
}
