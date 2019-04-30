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
        db.commit()
        db.close()
    }

    /**
     * Method to allow set the settings to certain values
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @param settings Array<out Pair<String, Any>>
     */
    fun setSettings(vararg settings: Pair<String, Any>) {
        settings.forEach { setting ->
            cache[setting.first] = setting.second
        }
        db.commit()
    }

    fun removeSettings(vararg settings: String) {
        settings.forEach { cache.remove(it) }
        db.commit()
    }
}