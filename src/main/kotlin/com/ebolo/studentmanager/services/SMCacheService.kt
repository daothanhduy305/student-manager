package com.ebolo.studentmanager.services

import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Service to handle operations regarding to caching / local db
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @property db DB
 * @property cache HTreeMap<String, Any>
 */
@Service
class SMCacheService {
    private lateinit var db: DB
    lateinit var cache: HTreeMap<String, Any>

    /**
     * Method to setup components needed for this module
     */
    @PostConstruct
    private fun setup() {
        db = DBMaker
            .fileDB(SMGlobal.CACHE_FILE)
            .fileMmapEnable()
            .transactionEnable() // to protect the db on sudden deaths
            .make()
        cache = db
            .hashMap(SMGlobal.CACHE_NAME, Serializer.STRING, Serializer.JAVA)
            .createOrOpen()
    }

    /**
     * Method to handle operations to do on destroying this module
     */
    @PreDestroy
    private fun onDestroyed() {
        db.close()
    }
}