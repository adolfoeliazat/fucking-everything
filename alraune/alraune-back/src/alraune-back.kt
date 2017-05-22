package alraune.back

import alraune.back.AlBackPile.log
import alraune.back.AlBackPile.pageTitle
import alraune.back.AlBackPile.t
import alraune.shared.OrderCreationForm
import alraune.shared.ShitPassedFromBackToFront
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
import java.io.FileInputStream
import java.nio.file.Paths
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import kotlin.properties.ReadOnlyProperty
import alraune.shared.*
import alraune.shared.AlCSS.Pack
import io.undertow.io.Receiver
import io.undertow.util.Methods
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.full.memberProperties


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

        ks.load(FileInputStream(AlBackPile.secrets.keyStore), AlBackPile.secrets.keyStorePassword.toCharArray())
        kmf.init(ks, AlBackPile.secrets.keyManagerPassword.toCharArray())
        sslContext.init(kmf.keyManagers, null, null)

        val backResourceHandler = Handlers.resource(PathResourceManager(Paths.get(AlBackPile.backResourceRootDir)))
        val frontResourceHandler = Handlers.resource(PathResourceManager(Paths.get(AlBackPile.frontOutDir)))
        val sharedKJSResourceHandler = Handlers.resource(PathResourceManager(Paths.get(AlBackPile.sharedKJSOutDir)))

        val port = 443
        val server = Undertow.builder()
            .addHttpsListener(port, "localhost", sslContext)
            .setHandler(object : HttpHandler {
                override fun handleRequest(exchange: HttpServerExchange) {
                    AlBackPile.httpServerExchange = exchange

                    val path = exchange.requestPath
                    log.debug("path = " + path)
                    log.debug("queryParameters = " + exchange.queryParameters)

                    val frontPathPrefix = "/alraune-front/"
                    val sharedKJSPathPrefix = "/shared-kjs/"

                    when {
                        path == "/alraune.css" -> {
                            exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/css; charset=utf-8")
                            exchange.responseSender.send(AlCSS_Back.sheet)
                        }
                        path.startsWith("/node_modules/") -> backResourceHandler.handleRequest(exchange)
                        path.startsWith(frontPathPrefix) -> {
                            exchange.relativePath = "/" + exchange.requestPath.substring(frontPathPrefix.length)
                            frontResourceHandler.handleRequest(exchange)
                        }
                        path.startsWith(sharedKJSPathPrefix) -> {
                            exchange.relativePath = "/" + exchange.requestPath.substring(sharedKJSPathPrefix.length)
                            sharedKJSResourceHandler.handleRequest(exchange)
                        }
                        else -> when (path) {
                            AlBackPile.orderCreationPagePath -> spitOrderFormPage(exchange)
                            else -> spitLandingPage(exchange)
                        }
                    }
                }

                private fun spitLandingPage(exchange: HttpServerExchange) {
                    spitUsualPage(AlPageID.landing, exchange) {o->
                        o- pageTitle("Fuck You")
                    }
                }

                private fun spitOrderFormPage(exchange: HttpServerExchange) {
                    val isPost = exchange.requestMethod == Methods.POST

                    fun onDataAvailable(data: OrderCreationForm) {
                        spitUsualPage(AlPageID.orderCreation, exchange) {o->
                            o- pageTitle(t("TOTE", "Заказ"))
                            o- kform{o->
                                fun addTextField(id: String, title: String, value: String, type: FieldType = FieldType.TEXT) {
                                    o- kdiv.className("form-group") {o->
                                        o- klabel(title)
                                        o- when (type) {
                                            FieldType.TEXT -> kinput(Attrs(type = "text", id = id, value = value, className = "form-control")) {}
                                            FieldType.TEXTAREA -> ktextarea(Attrs(id = id, rows = 5, className = "form-control"), value)
                                        }
                                    }
                                }

                                fun addTextField(prop: KProperty0<String>, title: String, type: FieldType = FieldType.TEXT) {
                                    addTextField(AlSharedPile.fieldDOMID(prop), title, prop.get(), type)
                                }

                                if (isPost) {
                                    val hasErrors = true
                                    if (hasErrors) {
                                        o- kdiv.className(AlCSS.errorBanner).text(t("TOTE", "Кое-что нужно исправить..."))
                                    }
                                }


                                addTextField(data::email, t("TOTE", "Почта"))
                                addTextField(data::name, t("TOTE", "Имя"))
                                addTextField(data::phone, t("TOTE", "Телефон"))
                                addTextField(data::documentTitle, t("TOTE", "Тема работы (задание)"))
                                addTextField(data::documentDetails, t("TOTE", "Детали"), FieldType.TEXTAREA)
                                o- kdiv{o->
                                    o- kbutton(Attrs(id = AlDomID.createOrderForm_submitButton, className = "btn btn-primary"), t("TOTE", "Вперед"))
                                    o- kdiv.id(AlDomID.ticker){}
                                }
                            }
                        }
                    }

                    if (isPost) {
                        val onSuccess = Receiver.FullStringCallback {exchange, dataString ->
                            log.debug("dataString = $dataString")
                            onDataAvailable(ObjectMapper().readValue(dataString, OrderCreationForm::class.java))
                        }
                        val onError = Receiver.ErrorCallback {exchange, e ->
                            log.error(e.message, e)
                            exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/html; charset=utf-8")
                            exchange.responseSender.send("Pizdets")
                        }
                        exchange.requestReceiver.receiveFullString(onSuccess, onError, Charsets.UTF_8)
                    } else {
                        val data = when {
                            AlBackDebug.messAroundBack201.should -> {
                                OrderCreationForm(email = "fuck",
                                                  name = "shit",
                                                  phone = "bitch",
                                                  documentTitle = "boobs",
                                                  documentDetails = "vagina")
                            }
                            else -> OrderCreationForm("", "", "", "", "")
                        }
                        onDataAvailable(data)
                    }
                }

                private fun spitUsualPage(pageID: String, exchange: HttpServerExchange, build: (Tag) -> Unit) {
                    val shit = ShitPassedFromBackToFront(
                        pageID = pageID,
                        postURL = "${AlBackPile.baseURL}${AlBackPile.orderCreationPagePath}")

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
                        ln("    <link rel='stylesheet' href='alraune.css'>")
                        ln("    <script>")
                        ln("        window['${shit::class.simpleName}'] = '${ObjectMapper().writeValueAsString(shit)}'")
                        ln("    </script>")
                        ln("</head>")
                        ln("<body>")
                        ln(AlSharedPile.beginContentMarker)
                        ln(kdiv.id(AlDomID.replaceableContent){o->
                            o- kdiv.className("container", build)
                        }.render())
                        ln(AlSharedPile.endContentMarker)
                        ln("")
                        ln("    <script src='/node_modules/jquery/dist/jquery.min.js'></script>")
                        ln("    <script src='/node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
                        ln("    <script src='/alraune-front/lib/kotlin.js'></script>")
                        ln("    <script src='/shared-kjs/shared-kjs.js?${System.currentTimeMillis()}'></script>")
                        ln("    <script src='/alraune-front/alraune-front.js?${System.currentTimeMillis()}'></script>")
                        ln("</body>")
                        ln("</html>")
                    })
                }
            }).build()
        server.start()
        log.info("Shit is spinning on port $port")
        File(AlBackPile.tmpDirPath + "/alraune-back-started").writeText("Fuck, yeah...")
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

object AlBackDebug {
    val messAroundBack201 by fuck()

    interface Fuck {
        val should: Boolean
    }

    fun fuck() = object : ReadOnlyProperty<Any?, Fuck> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Fuck {
            return object : Fuck {
                override val should: Boolean
                    get() = AlBackPile.httpServerExchange
                        .queryParameters["backMessAround"]?.contains(property.name) == true
            }
        }
    }
}

object AlCSS_Back {
    val sheet by lazy {
        val buf = StringBuilder()
        AlCSS.addShit(buf)

        for (prop in AlCSS::class.memberProperties) {
            if (prop.returnType.classifier == AlCSS.Pack::class) {
                val pack = prop.get(AlCSS) as AlCSS.Pack
                val selector = ".${prop.name}"
                pack.default?.let {buf.ln("$selector {$it}")}
                pack.link?.let {buf.ln("$selector:link {$it}")}
                pack.visited?.let {buf.ln("$selector:visited {$it}")}
                pack.hover?.let {buf.ln("$selector:hover {$it}")}
                pack.active?.let {buf.ln("$selector:active {$it}")}
                pack.focus?.let {buf.ln("$selector:focus {$it}")}
                pack.hoverActive?.let {buf.ln("$selector:hover:active {$it}")}
                pack.hoverFocus?.let {buf.ln("$selector:hover:focus {$it}")}
                pack.firstChild?.let {buf.ln("$selector:first-child {$it}")}
                pack.notFirstChild?.let {buf.ln("$selector:nth-child(1n+2) {$it}")}
            }
        }

        buf.toString()
    }
}






















