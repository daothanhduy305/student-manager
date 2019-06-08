package com.ebolo.studentmanager

import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.services.SMGlobal
import com.ebolo.studentmanager.services.Settings
import com.ebolo.studentmanager.views.SMInitFragment
import javafx.beans.property.SimpleBooleanProperty
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.PropertySource
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import tornadofx.*
import kotlin.collections.set
import kotlin.reflect.KClass


@SpringBootApplication
@EnableMongoRepositories
@EnableMongoAuditing
@PropertySource("classpath:version.properties")
class StudentManagerApplication : App(SMInitFragment::class) {
    private val logger = loggerFor(this.javaClass)

    var context: ConfigurableApplicationContext? = null
    lateinit var setupResult: SetupResult

    val db by lazy {
        DBMaker.fileDB(SMGlobal.CACHE_FILE)
            .fileMmapEnable()
            .fileMmapEnableIfSupported()
            .fileMmapPreclearDisable()
            .cleanerHackEnable()
            .closeOnJvmShutdown()
            .transactionEnable() // to protect the db on sudden deaths
            .make()
    }

    val cache: HTreeMap<String, Any> by lazy {
        db.hashMap(SMGlobal.CACHE_NAME, Serializer.STRING, Serializer.JAVA)
            .createOrOpen()
    }

    override fun init() {
        importStylesheet("/css/jfx-table-view.css")
        importStylesheet("/css/jfx-tab-pane.css")
        importStylesheet("/css/jfx-hamburger.css")

        currentApplication = this
    }

    override fun stop() {
        super.stop()
        db.commit()
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
            if (context != null && context!!.isActive) context!!.close()

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
    private fun checkDatabase() = hasSetting(Settings.DATABASE_NAME)
        && hasSetting(Settings.DATABASE_URI)

    private fun checkMasterAccount() = hasSetting(Settings.MASTER_ACCOUNT_USERNAME)
        && hasSetting(Settings.MASTER_ACCOUNT_PASSWORD)

    // endregion

    companion object {
        lateinit var currentApplication: StudentManagerApplication
        val isProcessingData = SimpleBooleanProperty(true)

        @JvmStatic
        fun main(args: Array<String>) {
            launch(StudentManagerApplication::class.java, *args)
        }

        /**
         * Method to allow set the settings to certain values
         *
         * @author ebolo
         * @since 0.0.1-SNAPSHOT
         *
         * @param settings Array<out Pair<String, Any>>
         */
        fun setSettings(vararg settings: Pair<String, Any>) {
            settings.forEach { setting ->
                currentApplication.cache[setting.first] = setting.second
            }
            currentApplication.db.commit()
        }

        /**
         * Method to remove settings from the app's cache
         *
         * @author ebolo
         * @since 0.0.1-SNAPSHOT
         *
         * @param settings Array<out String>
         */
        fun removeSettings(vararg settings: String) {
            settings.forEach { currentApplication.cache.remove(it) }
            currentApplication.db.commit()
        }

        /**
         * Method to get a setting from the app's cache
         *
         * @author ebolo
         * @since 0.0.1-SNAPSHOT
         *
         * @param settingName String
         * @return Optional<Any>
         */
        fun getSetting(settingName: String, default: Any? = null): Any? =
            if (currentApplication.cache.containsKey(settingName)) {
                currentApplication.cache[settingName]
            } else {
                default
            }

        /**
         * Method to check if the app's cache contains a setting entry for the settingsName
         *
         * @author ebolo
         * @since 0.0.1-SNAPSHOT
         *
         * @param settingsName String
         * @return Boolean
         */
        fun hasSetting(settingsName: String) = currentApplication.cache.containsKey(settingsName)
    }

    class SetupResult(val success: Boolean, val errors: List<SetupError>)

    enum class SetupError {
        NONE,
        DATABASE_ERROR,
        MASTER_ACCOUNT
    }
}