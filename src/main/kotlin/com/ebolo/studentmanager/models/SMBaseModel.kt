package com.ebolo.studentmanager.models

import com.ebolo.common.database.entities.EboloBaseEntity

interface SMBaseModel<E : EboloBaseEntity> {
    fun getEntity(): E
}