package com.ebolo.studentmanager.components

import com.ebolo.studentmanager.services.SMCacheService
import com.ebolo.studentmanager.services.Settings
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import javax.annotation.PostConstruct


@Configuration
class SMMongoConfiguration(
    private val cacheService: SMCacheService
) : AbstractMongoConfiguration() {
    private var mongoDB: String = "SMProject"
    private var mongoURI: String = "mongodb://ebolo:eboloyolo5653@localhost:27017/SMProject"

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
        /*cacheService.setSettings(
            Settings.DATABASE_NAME to mongoDB,
            Settings.DATABASE_URI to mongoURI
        )*/
    }

    override fun getDatabaseName(): String {
        // TODO Auto-generated method stub
        return mongoDB
    }

    @Throws(ClassNotFoundException::class)
    override fun mongoMappingContext(): MongoMappingContext {
        // TODO Auto-generated method stub
        return super.mongoMappingContext()
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(MongoClientURI(mongoURI))
    }
}