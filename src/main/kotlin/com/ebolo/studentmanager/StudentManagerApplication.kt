package com.ebolo.studentmanager

import com.ebolo.studentmanager.views.SMLoginFormView
import javafx.application.Application
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import tornadofx.*
import kotlin.reflect.KClass

@SpringBootApplication
@EnableMongoRepositories
@EnableMongoAuditing
class StudentManagerApplication : App(SMLoginFormView::class) {

    private lateinit var context: ConfigurableApplicationContext

    override fun init() {
        importStylesheet("/css/jfx-table-view.css")
        importStylesheet("/css/jfx-tab-pane.css")
        importStylesheet("/css/jfx-hamburger.css")
        this.context = SpringApplication.run(this.javaClass)
        context.autowireCapableBeanFactory.autowireBean(this)
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = context.getBean(type.java)
        }
    }

    override fun stop() {
        super.stop()
        context.close()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(StudentManagerApplication::class.java, *args)
        }
    }
}
