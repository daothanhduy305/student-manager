package com.ebolo.studentmanager

import com.ebolo.studentmanager.ebolo.utils.loggerFor
import com.ebolo.studentmanager.services.Settings
import com.ebolo.studentmanager.views.SMInitFragment
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.image.Image
import javafx.stage.Stage
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.PropertySource
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass


@SpringBootApplication
@EnableMongoRepositories
@EnableMongoAuditing
@EnableScheduling
@PropertySource("classpath:version.properties")
class StudentManagerApplication : App(SMInitFragment::class) {
    private val logger = loggerFor(this.javaClass)

    var springContext: ConfigurableApplicationContext? = null
    lateinit var setupResult: SetupResult

    override val configBasePath: Path
        get() = Paths.get("${System.getProperty("user.home")}/Applications/StudentManager/conf")

    override fun init() {
        importStylesheet(javaClass.getResource("/css/jfx-table-view.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/jfx-tab-pane.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/jfx-hamburger.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/jfx-generic.css").toExternalForm())

        currentApplication = this
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.icons.add(Image(javaClass.getResourceAsStream("/images/icon.png")))
    }

    override fun stop() {
        try {
            super.stop()
        } finally {
            if (springContext != null) {
                springContext!!.close()
            }
        }
    }

    /**
     * Method to setup the spring eco system
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    fun setupApp(uiContext: UIComponent): SetupResult {
        val listOfErrors = mutableListOf<SetupError>()
        this.setupResult = SetupResult(false, listOfErrors)


        uiContext.preferences {
            if (!getBoolean(Settings.DATABASE_SETUP, false)) {
                listOfErrors.add(SetupError.DATABASE_ERROR)
            }

            if (!getBoolean(Settings.MASTER_ACCOUNT_SETUP, false)) {
                listOfErrors.add(SetupError.MASTER_ACCOUNT)
            }
        }

        if (listOfErrors.isEmpty()) {
            if (springContext != null && springContext!!.isActive) springContext!!.close()

            uiContext.preferences {
                dbName = get(Settings.DATABASE_NAME, "")
                dbUri = get(Settings.DATABASE_URI, "")
            }

            springContext = SpringApplication.run(this.javaClass).apply {
                autowireCapableBeanFactory.autowireBean(this)
            }

            FX.dicontainer = object : DIContainer {
                override fun <T : Any> getInstance(type: KClass<T>): T = springContext!!.getBean(type.java)
            }

            syncCount.value = 0

            setupResult = SetupResult(true, listOf(SetupError.NONE))
        }

        return this.setupResult
    }

    companion object {
        lateinit var currentApplication: StudentManagerApplication
        var dbName: String = ""
        var dbUri: String = ""
        val syncCount = SimpleIntegerProperty(0)

        @JvmStatic
        fun main(args: Array<String>) {
            launch(StudentManagerApplication::class.java, *args)
        }

        /**
         * Method to increase the sync counter when there is a sync happening
         *
         * @author ebolo
         * @since 0.5.0
         *
         * @param numSync Int
         */
        fun startSync(numSync: Int = 1) {
            synchronized(syncCount) {
                syncCount += numSync
            }
        }

        /**
         * Method to decrease the sync counter whenever a sync is finished
         *
         * @author ebolo
         * @since 0.5.0
         */
        fun stopSync() {
            synchronized(syncCount) {
                if (syncCount > 0) syncCount.value--
            }
        }

        /**
         * Method to return whether there is still any sync in process
         *
         * @author ebolo
         * @since 0.5.0
         *
         * @return Boolean
         */
        fun isSyncing(): Boolean = syncCount > 0
    }

    class SetupResult(val success: Boolean, val errors: List<SetupError>)

    enum class SetupError(val friendlyMessage: String) {
        NONE(""),
        DATABASE_ERROR("Kết nối cơ sở dữ liệu thất bại"),
        MASTER_ACCOUNT("Thông tin tài khoản chủ không hợp lệ")
    }
}