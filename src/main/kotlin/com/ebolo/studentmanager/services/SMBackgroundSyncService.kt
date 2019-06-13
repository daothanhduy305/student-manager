package com.ebolo.studentmanager.services

import com.ebolo.studentmanager.ebolo.utils.loggerFor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tornadofx.*
import java.time.Instant
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Service to handle the background sync of the app
 *
 * @author ebolo
 * @since 0.5.0
 *
 * @property logger Logger
 * @property synced Int
 * @property subjectRefreshReg FXEventRegistration
 * @property classRefreshReg FXEventRegistration
 * @property studentRefreshReg FXEventRegistration
 * @property teacherRefreshReg FXEventRegistration
 */
@Service
class SMBackgroundSyncService : Controller() {
    private val logger = loggerFor(this::class.java)
    private var synced = 0

    private var subjectRefreshReg by singleAssign<FXEventRegistration>()
    private var classRefreshReg by singleAssign<FXEventRegistration>()
    private var studentRefreshReg by singleAssign<FXEventRegistration>()
    private var teacherRefreshReg by singleAssign<FXEventRegistration>()

    @PostConstruct
    private fun setup() {
        subjectRefreshReg = subscribe<SMSubjectRefreshEvent> { event -> handleSynced(event.source) }
        teacherRefreshReg = subscribe<SMTeacherRefreshEvent> { event -> handleSynced(event.source) }
        studentRefreshReg = subscribe<SMStudentRefreshEvent> { event -> handleSynced(event.source) }
        classRefreshReg = subscribe<SMClassListRefreshEvent> { event -> handleSynced(event.source) }
    }

    @PreDestroy
    private fun shutDown() {
        logger.info("Shutting down the background sync service")

        subjectRefreshReg.unsubscribe()
        teacherRefreshReg.unsubscribe()
        studentRefreshReg.unsubscribe()
        classRefreshReg.unsubscribe()
    }

    /**
     * Method to be run every one second to do the sync. First it will check if user allows background syncs.
     * Then it will check the interval to decide whether a sync should be run or not
     *
     * @author ebolo
     * @since 0.5.0
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 10000)
    fun doBackgroundRefresh() {
        logger.info("Checking sync settings")
        var useBgSync = false

        preferences {
            useBgSync = getBoolean(Settings.USE_BACGROUND_SYNC, false)
        }

        if (useBgSync) {
            var continueSync = false
            logger.info("Starting background sync")
            synchronized(synced) {
                if (synced == 0) {
                    synced = 4
                    continueSync = true
                } else {
                    logger.info("($synced) There might has been a sync in progress. Exiting and waiting...")
                }
            }

            if (continueSync) startSync()
        }
    }

    fun startSync() {
        val now = Instant.now()
        var lastSyncStamp = 0L
        var syncInterval = 0L

        preferences {
            syncInterval = getLong(Settings.SYNC_INTERVAL, SMGlobal.DEFAULT_SYNC_INTERVAL)
            lastSyncStamp = getLong(Settings.LAST_SYNC, now.toEpochMilli() - syncInterval)
        }

        if (lastSyncStamp <= now.toEpochMilli() - syncInterval) {
            logger.info("Syncing...")
            runLater {
                fire(SMSubjectRefreshRequest(source = "sync"))
                fire(SMTeacherRefreshRequest(source = "sync"))
                fire(SMStudentRefreshRequest(source = "sync"))
                fire(SMClassListRefreshRequest(source = "sync"))
            }
        } else {
            synchronized(synced) {
                synced = 0
            }
        }
    }

    /**
     * Method to handle the refresh events in the scheduled job
     *
     * @author ebolo
     * @since 0.5.0
     *
     * @param source String
     */
    private fun handleSynced(source: String) {
        if (source == "sync") {
            synchronized(synced) {
                if (synced > 0) synced--

                if (synced == 0) {
                    logger.info("Sync finished")

                    preferences {
                        putLong(Settings.LAST_SYNC, Instant.now().toEpochMilli())
                    }
                }
            }
        }
    }
}