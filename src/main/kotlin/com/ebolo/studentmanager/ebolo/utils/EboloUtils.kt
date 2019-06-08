package com.ebolo.studentmanager.ebolo.utils

import java.util.*

/**
 * Extension method to handle the Optional and transform the result when it is present
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @receiver Optional<T>
 * @param handler (T) -> R
 * @return R?
 */
fun <T : Any, R : Any?> Optional<T>.getWhenPresent(exceptionWhenNull: Exception? = null, handler: (T) -> R) =
    if (this.isPresent) handler.invoke(this.get()) else if (exceptionWhenNull != null) throw exceptionWhenNull else null

/**
 * Extension method to handle the Optional and transform the result for both cases present and not
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @receiver Optional<T>
 * @param ifPresentHandler (T) -> R
 * @param otherwise () -> R
 * @return R
 */
fun <T : Any, R : Any?> Optional<T>.getWhenPresentOr(ifPresentHandler: (T) -> R, otherwise: () -> R) =
    if (this.isPresent) ifPresentHandler.invoke(this.get()) else otherwise.invoke()

// region Enum

/**
 * Method to check if an enum contains a value
 *
 * @author mfulton26 (https://stackoverflow.com/users/3255152/mfulton26)
 * @since 0.0.1-SNAPSHOT
 *
 * @param name String
 * @return Boolean
 */
inline fun <reified T : Enum<T>> enumContains(name: String?): Boolean =
    !name.isNullOrBlank() && enumValues<T>().any { it.name == name }

/**
 * Method to try getting a value of enum
 *
 * @author mfulton26 (https://stackoverflow.com/users/3255152/mfulton26)
 * @author ebolo (daothanhduy305@gmail.com)
 *
 * @since 0.0.1-SNAPSHOT
 *
 * @param name String
 * @return T?
 */
inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): Optional<T> =
    Optional.ofNullable(enumValues<T>().find { it.name == name })

// endregion