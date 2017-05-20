package alraune.back

import alraune.back.AlBack.log
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
import vgrechka.*
import java.io.File
import io.undertow.server.HttpServerExchange
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.util.Headers
import org.slf4j.LoggerFactory
import java.io.FileInputStream
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

        val port = 443
        val server = Undertow.builder()
            .addHttpsListener(port, "localhost", sslContext)
            .setHandler(object : HttpHandler {
                override fun handleRequest(exchange: HttpServerExchange) {
                    log.debug("requestPath = " + exchange.requestPath)
                    log.debug("queryParameters = " + exchange.queryParameters)
                    exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/html; charset=utf-8")
                    exchange.responseSender.send(buildString {
                        ln("<!DOCTYPE html>")
                        ln("<html lang='en'>")
                        ln("<head>")
                        ln("    <meta charset='utf-8'>")
                        ln("    <meta http-equiv='X-UA-Compatible' content='IE=edge'>")
                        ln("    <meta name='viewport' content='width=device-width, initial-scale=1'>")
                        ln("    <meta name='google-signin-client_id' content='1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com'>")
                        ln("    <title>Alraune</title>")
//                        ln("    <script>")
//                        ln("        window.${ShitPassedFromBackToFront::class.simpleName} = {")
//                        ln("            ${ShitPassedFromBackToFront::pageID.name}: '${p.pageID}'")
//                        ln("        }")
//                        ln("    </script>")
                        ln("")
//                        ln("    <link href='node_modules/bootstrap/dist/css/bootstrap.min.css' rel='stylesheet'>")
//                        ln("    <link rel=\"stylesheet\" href=\"node_modules/font-awesome/css/font-awesome.min.css\">")
                        ln("</head>")
                        ln("<body>")
                        ln("    Пиздариус 2")
                        ln("")
//                        ln("    <script src='node_modules/jquery/dist/jquery.min.js'></script>")
//                        ln("    <script src='node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
//                        ln("    <script src='https://apis.google.com/js/api:client.js'></script>")
//                        ln("    <script src='out-front/lib/kotlin.js'></script>")
//                        ln("    <script src='symlinks/out/shared-x/shared-x.js$scriptSuffix'></script>")
//                        ln("    <script src='symlinks/out/shared-kjs/shared-kjs.js$scriptSuffix'></script>")
//                        ln("    <script src='symlinks/out/alraune-shared/alraune-shared.js$scriptSuffix'></script>")
//                        ln("    <script src='out-front/alraune-front.js$scriptSuffix'></script>")
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
//        server.handler = ServletHandler() - {o ->
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




