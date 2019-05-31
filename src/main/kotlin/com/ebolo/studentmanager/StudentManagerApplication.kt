package com.ebolo.studentmanager

import com.ebolo.common.utils.loggerFor
import com.ebolo.studentmanager.views.SMInitView
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

    private lateinit var context: ConfigurableApplicationContext

    override fun init() {
        importStylesheet("/css/jfx-table-view.css")
        importStylesheet("/css/jfx-tab-pane.css")
        importStylesheet("/css/jfx-hamburger.css")
    }

    override fun stop() {
        super.stop()
        context.close()
    }

    /**
     * Method to setup the spring eco system
     *
     * @author ebolo
     * @since 0.0.1-SNAPSHOT
     */
    fun setupApp() {
        this.context = SpringApplication.run(this.javaClass)
        context.autowireCapableBeanFactory.autowireBean(this)
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = context.getBean(type.java)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(StudentManagerApplication::class.java, *args)
        }
    }
}
