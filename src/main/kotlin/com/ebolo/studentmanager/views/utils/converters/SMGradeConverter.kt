package com.ebolo.studentmanager.views.utils.converters

import javafx.util.StringConverter

class SMGradeConverter : StringConverter<Int>() {
    /** {@inheritDoc}  */
    override fun fromString(value: String?): Int? {
        if (value == null) return null
        // If the specified value is null or zero-length, return null

        val trimmedValue = value.trim { it <= ' ' }

        return if (trimmedValue.isEmpty()) {
            -1
        } else Integer.valueOf(trimmedValue)

    }

    /** {@inheritDoc}  */
    override fun toString(value: Int?): String {
        // If the specified value is null, return a zero-length String
        return if (value == null || value < 0) {
            ""
        } else Integer.toString(value.toInt())

    }
}