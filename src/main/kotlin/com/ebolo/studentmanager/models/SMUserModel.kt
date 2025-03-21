package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMUserEntity
import tornadofx.*

class SMUserModel(
    item: SMUserDto? = null
) : SMBaseModel<SMUserEntity, SMUserModel.SMUserDto>(SMUserEntity::class, item) {

    // region dto
    /**
     * Class represents the user object with properties ready to be bind to the UI
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1-SNAPSHOT
     *
     * @property username (kotlin.String..kotlin.String?)
     * @property password (kotlin.String..kotlin.String?)
     */
    class SMUserDto : SMBaseDto() {
        var username by property<String>()
        fun usernameProperty() = getProperty(SMUserDto::username)

        var password by property<String>()
        fun passwordProperty() = getProperty(SMUserDto::password)
    }
    // endregion

    // region bindings
    val username = bind(SMUserDto::usernameProperty)
    val password = bind(SMUserDto::passwordProperty)
    // endregion

    override fun specificEntitySetup(entity: SMUserEntity) {
        entity.username = username.value
        entity.password = password.value
    }
}