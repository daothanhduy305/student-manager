package com.ebolo.studentmanager.components

import com.ebolo.studentmanager.services.SMCacheService
import com.ebolo.studentmanager.services.Settings
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import javax.annotation.PostConstruct


/**
 * Mongo template to be used in SM system
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property cacheService SMCacheService
 * @property mongoDB String
 * @property mongoURI String
 * @constructor
 */
@Configuration
class SMMongoConfiguration(
    private val cacheService: SMCacheService
) : AbstractMongoConfiguration() {
    private var mongoDB: String = ""
    private var mongoURI: String = ""

    @PostConstruct
    fun setup() {
        val mongoDbName = cacheService.cache[Settings.DATABASE_NAME] as String?
        if (mongoDbName != null) {
            mongoDB = mongoDbName
        }

        val mongoDbUri = cacheService.cache[Settings.DATABASE_URI] as String?
        if (mongoDbUri != null) {
            mongoURI = mongoDbUri
        }
        // Uncomment the below block to allow resetting the db configurations
        /*cacheService.setSettings(
            Settings.DATABASE_NAME to mongoDB,
            Settings.DATABASE_URI to mongoURI
        )*/
    }

    override fun getDatabaseName(): String {
        return mongoDB
    }

    @Throws(ClassNotFoundException::class)
    override fun mongoMappingContext(): MongoMappingContext {
        return super.mongoMappingContext()
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(MongoClientURI(mongoURI))
    }
}