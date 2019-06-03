package com.ebolo.studentmanager.components

import com.ebolo.studentmanager.StudentManagerApplication
import com.ebolo.studentmanager.services.SMGlobal
import com.ebolo.studentmanager.services.Settings
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

/**
 * Custom auditor aware for the SM project. Basically, after the usere has been logged into the system successfully,
 * we can retrieve the current logged in user from cache
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @property cacheService SMCacheService
 * @constructor
 */
@Component
class SMAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor() = Optional.of(
        StudentManagerApplication
            .getSetting(Settings.LOGGING_USER, SMGlobal.SYSTEM_USER) as String
    )
}