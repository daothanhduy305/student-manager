package com.ebolo.studentmanager.services

import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Service
class SMCacheService {
    private lateinit var db: DB
    lateinit var cache: HTreeMap<String, Any>

    @PostConstruct
    private fun setup() {
        db = DBMaker
            .fileDB(SMGlobal.CACHE_FILE)
            .fileMmapEnable()
            .make()
        cache = db
            .hashMap(SMGlobal.CACHE_NAME, Serializer.STRING, Serializer.JAVA)
            .createOrOpen()
    }

    @PreDestroy
    private fun onDestroyed() {
        db.close()
    }
}