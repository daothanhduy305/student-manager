package com.ebolo.studentmanager.services

object SMGlobal {
    // region cache
    const val CACHE_FILE = "SM.db"
    const val CACHE_NAME = "SM_Cache"

    const val CACHE_ENTRY_LOGGING_USER = "logging_user"
    // endregion

    // region system
    const val SYSTEM_USER = "System"
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
     * Save the password string into the cache - TODO this must save the hashed pw instead
     */
    const val CREDENTIAL_PASSWORD = "CREDENTIAL_PASSWORD"
}