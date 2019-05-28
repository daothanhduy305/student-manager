package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseUserEntity
import com.ebolo.common.utils.reflect.unsafeCastTo
import com.ebolo.studentmanager.models.SMUserModel
import org.springframework.data.mongodb.core.mapping.Document

@Document("Users")
open class SMUserEntity : EboloBaseUserEntity(), SMIEntity<SMUserModel.SMUserDto> {
    override fun toDto(): SMUserModel.SMUserDto = this unsafeCastTo SMUserModel.SMUserDto::class
}