package com.ebolo.studentmanager.utils

class SMCRUDUtils {
    class SMCRUDResult(
        val success: Boolean = true,
        val errorMessage: String = ""
    )

    enum class CRUDMode {
        NEW,
        EDIT,
        DELETE
    }
}