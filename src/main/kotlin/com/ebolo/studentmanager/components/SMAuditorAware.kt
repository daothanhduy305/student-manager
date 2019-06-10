package com.ebolo.studentmanager.components

import com.ebolo.studentmanager.services.SMGlobal
import com.ebolo.studentmanager.services.Settings
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import tornadofx.*
import java.util.*

/**
 * Custom auditor aware for the SM project. Basically, after the user has been logged into the system successfully,
 * we can retrieve the current logged in user from cache
 *
 * @author ebolo (daothanhduy305@gmail.com)
 * @since 0.0.1-SNAPSHOT
 *
 * @constructor
 */
@Component
class SMAuditorAware : AuditorAware<String>, Controller() {
    override fun getCurrentAuditor() = Optional.of(
        {
            var currentAuditor = SMGlobal.SYSTEM_USER

            preferences {
                currentAuditor = get(Settings.LOGGING_USER, SMGlobal.SYSTEM_USER)
            }

            currentAuditor
        }()
    )
}