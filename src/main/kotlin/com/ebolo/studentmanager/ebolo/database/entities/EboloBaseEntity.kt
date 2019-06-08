package com.ebolo.studentmanager.ebolo.database.entities

import org.springframework.data.annotation.*
import java.io.Serializable
import java.time.Instant

/**
 * Base entity class
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property otherInfo would hold all the dynamic info and should have the value of string or toString() implementation
 */
open class EboloBaseEntity : Serializable {
    @Id
    lateinit var id: String
    @CreatedDate
    lateinit var createdTime: Instant
    @LastModifiedDate
    lateinit var lastModifiedTime: Instant
    @CreatedBy
    lateinit var createdBy: String
    @LastModifiedBy
    lateinit var lastModifiedBy: String
    var otherInfo: MutableMap<String, Any> = mutableMapOf()
}