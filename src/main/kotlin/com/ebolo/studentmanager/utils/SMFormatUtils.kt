package com.ebolo.studentmanager.utils

import com.ebolo.studentmanager.services.SMGlobal
import tornadofx.*

/**
 * Method to format a string to a decimal number with format #,###
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @receiver String
 * @return (kotlin.String..kotlin.String?)
 */
fun String.formatDecimal(): String {
    val strippedString = this.replace("[^\\d]".toRegex(), "").trim()
    return if (strippedString.isBlank()) ""
    else SMGlobal.decimalFormatter.format(strippedString.toLong())
}

/**
 * Method to check if a string is a number (maybe in any format)
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @receiver String
 * @return Boolean
 */
fun String.isFormattedLong() = this.replace("[^\\d]".toRegex(), "")
    .trim()
    .isLong()