package com.ebolo.studentmanager.services

import java.text.DecimalFormat

object SMGlobal {
    // region cache
    const val CACHE_FILE = "SM.db"
    const val CACHE_NAME = "SM_Cache"

    const val CACHE_ENTRY_LOGGING_USER = "logging_user"
    // endregion

    // region system
    const val SYSTEM_USER = "System"
    val decimalFormatter = DecimalFormat("#,###")
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
     * Save a master account into the cache - God mode
     */
    const val MASTER_ACCOUNT_USERNAME = "MASTER_USER_NAME"
    /**
     * Save a master password into the cache
     */
    const val MASTER_ACCOUNT_PASSWORD = "MASTER_PASSWORD"
}