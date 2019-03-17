package com.ebolo.studentmanager.components

import com.ebolo.studentmanager.services.SMCacheService
import com.ebolo.studentmanager.services.SMGlobal
import org.springframework.beans.factory.annotation.Autowired
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
class SMAuditorAware(
    @Autowired private val cacheService: SMCacheService
) : AuditorAware<String> {
    override fun getCurrentAuditor() = Optional
        .of(cacheService.cache.getOrDefault(SMGlobal.CACHE_ENTRY_LOGGING_USER, "") as String)
}