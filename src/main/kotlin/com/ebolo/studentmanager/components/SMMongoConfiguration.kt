package com.ebolo.studentmanager.components

import com.ebolo.studentmanager.StudentManagerApplication
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.core.mapping.MongoMappingContext


/**
 * Mongo template to be used in SM system
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property mongoDB String
 * @property mongoURI String
 * @constructor
 */
@Configuration
class SMMongoConfiguration : AbstractMongoConfiguration() {
    override fun getDatabaseName(): String {
        return StudentManagerApplication.dbName
    }

    @Throws(ClassNotFoundException::class)
    override fun mongoMappingContext(): MongoMappingContext {
        return super.mongoMappingContext()
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(MongoClientURI(StudentManagerApplication.dbUri))
    }
}