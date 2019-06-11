package com.ebolo.studentmanager.services

import java.text.DecimalFormat

object SMGlobal {
    const val APP_NAME = "Student Manager"

    // region cache
    const val CACHE_FILE = "SM.db"
    const val CACHE_NAME = "SM_Cache"
    // endregion

    // region system
    const val SYSTEM_USER = "System"
    val decimalFormatter = DecimalFormat("#,###")
    const val DEFAULT_SYNC_INTERVAL = 10000L
    // endregion
}

object Settings {
    /**
     * This is a boolean indicates if the credential is saved into the cache
     */
    const val REMEMBER_CREDENTIAL = "REMEMBER_CREDENTIAL"
    /**
     * Save the username string into the cache
     */
    const val CREDENTIAL_USERNAME = "CREDENTIAL_USERNAME"
    /**
     * Save the password string into the cache
     */
    const val CREDENTIAL_PASSWORD = "CREDENTIAL_PASSWORD"
    /**
     * Save the database name into the cache
     */
    const val DATABASE_NAME = "DATABASE_NAME"
    /**
     * Save the database uri into the cache
     */
    const val DATABASE_URI = "DATABASE_URI"
    /**
     * Indicates whether the db has been setup or not
     */
    const val DATABASE_SETUP = "DATABASE_SETUP"
    /**
     * Save a master account into the cache - God mode
     */
    const val MASTER_ACCOUNT_USERNAME = "MASTER_USER_NAME"
    /**
     * Save a master password into the cache
     */
    const val MASTER_ACCOUNT_PASSWORD = "MASTER_PASSWORD"
    /**
     * Indicates whether the master account has been setup or not
     */
    const val MASTER_ACCOUNT_SETUP = "MASTER_ACCOUNT_SETUP"
    /**
     * Current using being logged into the system
     */
    const val LOGGING_USER = "LOGGING_USER"
    /**
     * Indicates whether the app is being run in god mode
     */
    const val GOD_MODE = "GOD_MODE"
    /**
     * Turn on or off background sync feature
     */
    const val USE_BACGROUND_SYNC = "BACKGROUND_SYNC"
    /**
     * Interval for the background sync
     */
    const val SYNC_INTERVAL = "SYNC_INTERVAL"
    /**
     * Last sync stamp
     */
    const val LAST_SYNC = "LAST_SYNC"
}