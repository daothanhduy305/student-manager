package com.ebolo.studentmanager.ebolo.database.entities

import java.time.Instant

/**
 * Base user entity class for systems
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @see EboloBaseEntity
 */
open class EboloBaseUserEntity(
    var username: String = "",
    var email: String = "",
    var password: String? = null,
    var firebaseUid: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var fullName: String? = null,
    var avatarUrl: String? = null,
    var phone: String? = null,
    var address: String? = null,
    var birthday: Instant? = null,
    var gender: String = Gender.UNKNOWN.name,
    var fcmKeyList: MutableSet<String> = mutableSetOf(),
    var roles: MutableSet<String> = mutableSetOf(EboloRole.ROLE_USER.name)
) : EboloBaseEntity()

/**
 * Enum class of legit genders used within the system
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 */
enum class Gender(val friendlierName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHERS("Others"),
    UNKNOWN("Un-disclosed")
}

/**
 * Default roles
 */
enum class EboloRole {
    ROLE_USER,
    ROLE_ADMIN
}