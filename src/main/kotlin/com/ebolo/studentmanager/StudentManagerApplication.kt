package com.ebolo.studentmanager

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.services.SMGlobal
import com.ebolo.studentmanager.services.Settings
import com.ebolo.studentmanager.views.SMInitView
import org.mapdb.DBMaker
import org.mapdb.Serializer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import tornadofx.*
import kotlin.reflect.KClass

@SpringBootApplication
@EnableMongoRepositories
@EnableMongoAuditing
@ComponentScan(basePackages = [
    "com.ebolo.common.configs.components",
    "com.ebolo.common.services",
    "com.ebolo.studentmanager"
])
class StudentManagerApplication : App(SMInitView::class) {
    private val logger = loggerFor(this.javaClass)

    var context: ConfigurableApplicationContext? = null
    lateinit var setupResult: SetupResult

    override fun init() {
        importStylesheet("/css/jfx-table-view.css")
        importStylesheet("/css/jfx-tab-pane.css")
        importStylesheet("/css/jfx-hamburger.css")
    }

    override fun stop() {
        super.stop()
        if (context != null) {
            context!!.close()
        }
    }

    /**
     * Method to setup the spring eco system
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    fun setupApp(): SetupResult {
        val listOfErrors = mutableListOf<SetupError>()

        if (!checkDatabase()) {
            listOfErrors.add(SetupError.DATABASE_ERROR)
        }

        if (!checkMasterAccount()) {
            listOfErrors.add(SetupError.MASTER_ACCOUNT)
        }

        this.setupResult = SetupResult(false, listOfErrors)

        if (listOfErrors.isEmpty()) {
            this.context = SpringApplication.run(this.javaClass).apply {
                autowireCapableBeanFactory.autowireBean(this)
            }

            FX.dicontainer = object : DIContainer {
                override fun <T : Any> getInstance(type: KClass<T>): T = context!!.getBean(type.java)
            }

            this.setupResult = SetupResult(true, listOf(SetupError.NONE))
        }

        return this.setupResult
    }

    // region setup check

    /**
     * private method to check if the database has been configured properly
     *
     * @author ebolo
     * @since 0.0.1-SNPASHOT
     *
     * @return Boolean
     */
    private fun checkDatabase(): Boolean {
        var result = false

        with(DBMaker
            .fileDB(SMGlobal.CACHE_FILE)
            .fileMmapEnable()
            .transactionEnable() // to protect the db on sudden deaths
            .make()
        ) {
            val cache = this.hashMap(SMGlobal.CACHE_NAME, Serializer.STRING, Serializer.JAVA)
                .createOrOpen()

            if (cache.containsKey(Settings.DATABASE_NAME) && cache.containsKey(Settings.DATABASE_URI)) {
                result = true
            }

            this.commit()
            this.close()
        }

        return result
    }

    private fun checkMasterAccount(): Boolean {
        var result = false

        with(DBMaker
            .fileDB(SMGlobal.CACHE_FILE)
            .fileMmapEnable()
            .transactionEnable() // to protect the db on sudden deaths
            .make()
        ) {
            val cache = this.hashMap(SMGlobal.CACHE_NAME, Serializer.STRING, Serializer.JAVA)
                .createOrOpen()

            if (cache.containsKey(Settings.MASTER_ACCOUNT_USERNAME) && cache.containsKey(Settings.MASTER_ACCOUNT_PASSWORD)) {
                result = true
            }

            this.commit()
            this.close()
        }

        return result
    }

    // endregion

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(StudentManagerApplication::class.java, *args)
        }
    }

    class SetupResult(val success: Boolean, val errors: List<SetupError>)

    enum class SetupError {
        NONE,
        DATABASE_ERROR,
        MASTER_ACCOUNT
    }
}