package alraune.back

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import vgrechka.db.*

@Suppress("unused")
@EnableJpaRepositories
@ComponentScan(basePackages = arrayOf("alraune"))
abstract class AlrauneBaseAppConfig : BasePostgresAppConfig(entityPackagesToScan = arrayOf("alraune")) {
}

open class AlrauneProdAppConfig : AlrauneBaseAppConfig() {
    override val dbConnectionParams by lazy {AlBackPile0.secrets.db.prod}
}

open class AlrauneTestAppConfig : AlrauneBaseAppConfig() {
    override val dbConnectionParams by lazy {AlBackPile0.secrets.db.test}
}


class AlrauneLogConfigurator : ContextAwareBase(), Configurator {

    class ShortLevelConverter : ClassicConverter() {
        override fun convert(le: ILoggingEvent): String {
            return le.level.toString().substring(0, 1)
        }
    }

    override fun configure(lc: LoggerContext) {
        run { // Default
            val ca = ConsoleAppender<ILoggingEvent>()
            ca.context = lc
            ca.name = "console"
            val encoder = LayoutWrappingEncoder<ILoggingEvent>()
            encoder.context = lc


            val layout = PatternLayout()
            layout.setPattern("%-5level %logger{36} - %msg%n")
            layout.context = lc
            layout.start()

            encoder.layout = layout
            ca.encoder = encoder
            ca.start()

            val rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
            rootLogger.level = Level.INFO
            rootLogger.addAppender(ca)
        }

        run { // net.ttddyy.dsproxy
            val ca = ConsoleAppender<ILoggingEvent>()
            ca.context = lc
            ca.name = "console"
            val encoder = LayoutWrappingEncoder<ILoggingEvent>()
            encoder.context = lc

            val layout = PatternLayout()
            layout.setPattern("%msg%n")
            layout.context = lc
            layout.start()

            encoder.layout = layout
            ca.encoder = encoder
            ca.start()

            val logger = lc.getLogger("net.ttddyy.dsproxy")
            logger.level = Level.INFO
            logger.isAdditive = false
            logger.addAppender(ca)
        }

        run { // Alraune
            val ca = ConsoleAppender<ILoggingEvent>()
            ca.context = lc
            ca.name = "alrauneConsole"
            val encoder = LayoutWrappingEncoder<ILoggingEvent>()
            encoder.context = lc

            val layout = PatternLayout()
            layout.getInstanceConverterMap().put("shortLevel", ShortLevelConverter::class.java.name)
            layout.pattern = "[Alraune-%shortLevel] %msg%n"
            layout.context = lc
            layout.start()

            encoder.layout = layout
            ca.encoder = encoder
            ca.start()

            val logger = lc.getLogger("alraune")
            logger.level = Level.DEBUG
            logger.isAdditive = false
            logger.addAppender(ca)
        }
    }
}
