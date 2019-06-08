package com.ebolo.studentmanager.ebolo.utils

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * Copy the existing property from source to destination and skip any
 * mis-matched property (might be due to the non-existence or type incompatibility)
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @param destination
 * @param skippNulls            indicates whether we should skip the null property or not
 *                              this would make the process considerably safer especially
 *                              when Kotlin has type-safe null protection mechanism
 * @param skippingProperties    list of skipping properties
 * @param preProcessedValues    map of pre-processed values that can be used instead
 *
 * @return the an object with copied properties from source in type [T]
 * @throws Exception when the source is primary type
 */
fun <T : Any> Any.copyProperties(
    destination: T,
    skippNulls: Boolean = false,
    skippingProperties: List<String> = listOf(),
    preProcessedValues: Map<String, Any?> = mapOf()
) = try {
    destination.apply {
        this@copyProperties::class.memberProperties
            .filter { !skippingProperties.contains(it.name) }
            .forEach { property ->
                try {
                    val value = Optional.ofNullable(
                        if (property.name in preProcessedValues.keys)
                            preProcessedValues[property.name]
                        else property.getter.call(this@copyProperties)
                    )
                    if (value.isPresent || !skippNulls) {
                        val entityProperty = destination::class
                            .memberProperties
                            .first { it.name == property.name } as KMutableProperty<*>
                        entityProperty.setter.call(destination, value.get())
                    }
                } catch (ignored: Exception) { /*Val properties*/
                }
            }
    }
} catch (e: Error) {
    throw Exception("Primary types are not supported")
    // This would be from the cases of primary objects...We don't care
}

/**
 * Dangerously cast from an object to another type one (i.e from parent to child)
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @param to    destination class
 *
 * @return the an object casted from [this] to class [T]
 */
infix fun <T : Any> Any.unsafeCastTo(to: KClass<T>) =
    this.copyProperties(
        destination = to.java.newInstance()
    ) ?: throw Exception()