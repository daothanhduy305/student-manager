package com.ebolo.studentmanager.entities

import com.ebolo.studentmanager.models.SMBaseModel

/**
 * Interface to contains fundamental operations applied to the entities of this project
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 *
 * @param D: SMBaseModel.SMBaseDto
 */
interface SMIEntity<D : SMBaseModel.SMBaseDto> {
    /**
     * Method to convert an entity object to its relevant dto counterpart
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     *
     * @return D
     */
    fun toDto(): D
}