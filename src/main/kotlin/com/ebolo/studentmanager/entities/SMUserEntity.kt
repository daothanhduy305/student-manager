package com.ebolo.studentmanager.entities

import com.ebolo.common.database.entities.EboloBaseUserEntity
import org.springframework.data.mongodb.core.mapping.Document

@Document("Users")
open class SMUserEntity : EboloBaseUserEntity()