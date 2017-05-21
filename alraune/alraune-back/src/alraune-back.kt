package alraune.back

import alraune.back.AlBack.log
import alraune.back.AlBackPile.pageTitle
import alraune.back.AlBackPile.t
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import com.fasterxml.jackson.databind.ObjectMapper
import io.undertow.Handlers
import vgrechka.*
import java.io.File
import io.undertow.server.HttpServerExchange
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.util.Headers
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.nio.file.Paths
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext


object AlBack {
    val log = LoggerFactory.getLogger(AlBack::class.java)
    val tmpDirPath = "c:/tmp"

    val secrets by lazy {
        // TODO:vgrechka Get file name from environment variable
        ObjectMapper().readValue(File("e:/fpebb/alraune/alraune-secrets.json"), JSON_AlrauneSecrets::class.java)!!
    }
}


@Ser class JSON_AlrauneSecrets(
    val keyStore: String,
    val keyStorePassword: String,
    val keyManagerPassword: String
)

object StartAlrauneBack {
    @JvmStatic
    fun main(args: Array<String>) {
        runUndertow()
    }

    private fun runUndertow() {
        val sslContext = SSLContext.getInstance("TLS")
        val kmf = KeyManagerFactory.getInstance("SunX509")
        val ks = KeyStore.getInstance("JKS")

        ks.load(FileInputStream(AlBack.secrets.keyStore), AlBack.secrets.keyStorePassword.toCharArray())
        kmf.init(ks, AlBack.secrets.keyManagerPassword.toCharArray())
        sslContext.init(kmf.keyManagers, null, null)

        val resourceHandler = Handlers.resource(PathResourceManager(Paths.get(AlBackPile.rootDir)))

        val port = 443
        val server = Undertow.builder()
            .addHttpsListener(port, "localhost", sslContext)
            .setHandler(object : HttpHandler {
                override fun handleRequest(exchange: HttpServerExchange) {
                    val path = exchange.requestPath
                    log.debug("path = " + path)
                    log.debug("queryParameters = " + exchange.queryParameters)

                    if (path.startsWith("/node_modules/")) {
                        return resourceHandler.handleRequest(exchange)
                    }

                    exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/html; charset=utf-8")
                    exchange.responseSender.send(buildString {
                        ln("<!DOCTYPE html>")
                        ln("<html lang='en'>")
                        ln("<head>")
                        ln("    <meta charset='utf-8'>")
                        ln("    <meta http-equiv='X-UA-Compatible' content='IE=edge'>")
                        ln("    <meta name='viewport' content='width=device-width, initial-scale=1'>")
                        ln("    <title>Alraune</title>")
                        ln("")
                        ln("    <link href='/node_modules/bootstrap/dist/css/bootstrap.min.css' rel='stylesheet'>")
                        ln("    <link rel='stylesheet' href='/node_modules/font-awesome/css/font-awesome.min.css'>")
                        ln("</head>")
                        ln("<body>")
                        ln(kdiv{o->
                            o- kdiv.className("container"){o->
                                o- pageTitle(t("TOTE", "Заказ"))
                                o- kform{o->
                                    fun addTextField(name: String, title: String, value: String, type: FieldType = FieldType.TEXT) {
                                        o- kdiv.className("form-group"){o->
                                            o- klabel(title)
                                            o- when (type) {
                                                FieldType.TEXT -> kinput(Attrs(type = "text", name = name, value = value, className = "form-control")) {}
                                                FieldType.TEXTAREA -> ktextarea(Attrs(name = name, rows = 5, className = "form-control"), value)
                                            }
                                        }
                                    }

                                    addTextField("email", t("TOTE", "Почта"), "iperdonde@mail.com")
                                    addTextField("name", t("TOTE", "Имя"), "Иммануил Пердондэ")
                                    addTextField("phone", t("TOTE", "Телефон"), "+38 (068) 4542823")
                                    addTextField("paperTitle", t("TOTE", "Тема работы (задание)"), "Как я пинал хуи на практике")
                                    addTextField("paperDetails", t("TOTE", "Детали"), "Детали? Я ебу, какие там детали...", FieldType.TEXTAREA)
                                    o- kbutton(Attrs(className = "btn btn-primary"), t("TOTE", "Вперед"))
                                }
                            }
                        }.render())
                        ln("")
                        ln("    <script src='/node_modules/jquery/dist/jquery.min.js'></script>")
                        ln("    <script src='/node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
//                        ln("    <script src='out-front/lib/kotlin.js'></script>")
                        ln("</body>")
                        ln("</html>")
                    })
                }
            }).build()
        server.start()
        log.info("Shit is spinning on port $port")
        File(AlBack.tmpDirPath + "/alraune-back-started").writeText("Fuck, yeah...")
    }



//    private fun runJetty() {
//        class FuckingServlet : HttpServlet() {
//            override fun service(req: HttpServletRequest, res: HttpServletResponse) {
//                req.characterEncoding = "UTF-8"
//                req.queryString
//                res.contentType = "text/html; charset=utf-8"
//                res.writer.println("Fuck you")
//                res.status = HttpServletResponse.SC_OK
//            }
//        }
//
//        //        val httpPort = 80
//        val httpsPort = 443
//        val port = httpsPort
//
//        val server = Server()
//
//        //        val httpConnector = ServerConnector(server)
//        //        httpConnector.setPort(httpPort)
//
//        val https = HttpConfiguration()
//        https.addCustomizer(SecureRequestCustomizer())
//
//        val sslContextFactory = SslContextFactory()
//        sslContextFactory.keyStorePath = AlBack.secrets.keyStore
//        sslContextFactory.setKeyStorePassword(AlBack.secrets.keyStorePassword)
//        sslContextFactory.setKeyManagerPassword(AlBack.secrets.keyManagerPassword)
//
//        val httpsConnector = ServerConnector(server,
//                                             SslConnectionFactory(sslContextFactory, "http/1.1"),
//                                             HttpConnectionFactory(https))
//        httpsConnector.port = httpsPort
//
//        server.connectors = arrayOf(/*httpConnector,*/ httpsConnector)
//
//        server.handler = ServletHandler()-{o->
//            o.addServletWithMapping(ServletHolder(FuckingServlet()), "/*")
//        }
//        server.start()
//
//        clog("[Alraune] Shit is spinning on port $port")
//        server.join()
//    }
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
            rootLogger.addAppender(ca)
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
            logger.isAdditive = false
            logger.addAppender(ca)
        }
    }
}




