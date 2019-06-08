package com.ebolo.studentmanager.ebolo.utils

import org.slf4j.LoggerFactory

/**
 * Get the logger for the current class
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @param clazz Class<T>
 * @return (org.slf4j.Logger..org.slf4j.Logger?)
 */
fun <T> loggerFor(clazz: Class<T>) = LoggerFactory.getLogger(clazz)!!

/**
 * Get the logger for a class name
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @param clazz String
 * @return Logger
 */
fun loggerFor(clazz: String) = LoggerFactory.getLogger(clazz)!!