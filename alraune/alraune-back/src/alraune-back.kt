package alraune.back

import alraune.back.AlBackPile0.log
import alraune.back.AlRenderPile.pageTitle
import alraune.back.AlRenderPile.renderOrderTitle
import alraune.back.AlRenderPile.t
import alraune.shared.OrderCreationForm
import alraune.shared.ShitPassedFromBackToFront
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
import io.undertow.io.Receiver
import io.undertow.util.Methods
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.full.memberProperties

object StartAlrauneBack {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(AlrauneTestAppConfig::class.java)
        runUndertow()
    }

    private fun runUndertow() {
        val sslContext = SSLContext.getInstance("TLS")
        val kmf = KeyManagerFactory.getInstance("SunX509")
        val ks = KeyStore.getInstance("JKS")

        val secrets = AlBackPile0.secrets
        ks.load(FileInputStream(secrets.keyStore), secrets.keyStorePassword.toCharArray())
        kmf.init(ks, secrets.keyManagerPassword.toCharArray())
        sslContext.init(kmf.keyManagers, null, null)

        val backResourceHandler = Handlers.resource(PathResourceManager(Paths.get(AlBackPile0.backResourceRootDir)))
        val frontResourceHandler = Handlers.resource(PathResourceManager(Paths.get(AlBackPile0.frontOutDir)))
        val sharedKJSResourceHandler = Handlers.resource(PathResourceManager(Paths.get(AlBackPile0.sharedKJSOutDir)))

        val port = 443
        val server = Undertow.builder()
            .addHttpsListener(port, "localhost", sslContext)
            .setHandler(object : HttpHandler {
                override fun handleRequest(exchange: HttpServerExchange) {
                    val ctx = AlRequestContext()
                    ctx.exchange = exchange

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
                            AlBackPile0.orderCreationPagePath -> spitOrderFormPage(ctx)
                            else -> spitLandingPage(ctx)
                        }
                    }
                }

                private fun spitLandingPage(ctx: AlRequestContext) {
                    spitUsualPage(AlPageID.landing, ctx) {o->
                        o- pageTitle("Fuck You")
                    }
                }

                private fun spitOrderFormPage(ctx: AlRequestContext) {
                    val isPost = ctx.exchange.requestMethod == Methods.POST

                    fun onDataAvailable(data: OrderCreationForm) {
                        spitUsualPage(AlPageID.orderCreation, ctx) {o->
                            val q = AlBackPile
                            var hasErrors = false
                            val fieldRenderers = mutableListOf<() -> Unit>()

                            fun declareField(prop: KProperty0<String>,
                                             title: String, validator: (String?) -> ValidationResult,
                                             fieldType: FieldType = FieldType.TEXT): String {
                                val vr = validator(prop.get())
                                val theError = when {
                                    isPost -> vr.error
                                    else -> null
                                }
                                if (theError != null)
                                    hasErrors = true

                                fieldRenderers += {
                                    val id = AlSharedPile.fieldDOMID(name = prop.name)
                                    o- kdiv.className("form-group") {o->
                                        if (theError != null)
                                            o.amend(Style(marginBottom = "0"))
                                        o- klabel(text = title)
                                        val control = when (fieldType) {
                                            FieldType.TEXT -> kinput(Attrs(type = "text", id = id, value = vr.sanitizedString, className = "form-control")) {}
                                            FieldType.TEXTAREA -> ktextarea(Attrs(id = id, rows = 5, className = "form-control"), text = vr.sanitizedString)
                                        }
                                        o- kdiv(Style(position = "relative")){o->
                                            o- control
                                            if (theError != null) {
                                                o- kdiv(Style(marginTop = "5px", marginRight = "9px", textAlign = "right", color = "${Color.RED_700}"))
                                                    .text(theError)
                                                // TODO:vgrechka Shift red circle if control has scrollbar
                                                o- kdiv(Style(width = "15px", height = "15px", backgroundColor = "${Color.RED_300}",
                                                              borderRadius = "10px", position = "absolute", top = "10px", right = "8px"))
                                            }
                                        }
                                    }
                                }

                                return vr.sanitizedString
                            }

                            val f = AlFields.order
                            val email = declareField(data::email, f.email.title, q::validateEmail)
                            val contactName = declareField(data::name, f.contactName.title, q::validateName)
                            val phone = declareField(data::phone, f.phone.title, q::validatePhone)
                            val documentTitle = declareField(data::documentTitle, f.documentTitle.title, q::validateDocumentTitle)
                            val documentDetails = declareField(data::documentDetails, f.documentDetails.title, q::validateDocumentDetails, FieldType.TEXTAREA)

                            if (!isPost || hasErrors) {
                                ctx.shit.documentCategoryID = "101"
                                o- pageTitle(t("TOTE", "Заказ"))
                                o- kform{o->
                                    if (hasErrors)
                                        o- kdiv.className(AlCSS.errorBanner).text(t("TOTE", "Кое-что нужно исправить..."))
                                    for (renderField in fieldRenderers)
                                        renderField()
                                    o- kdiv.className("form-group"){o->
                                        o- klabel(text = f.documentCategory.title)
                                        o- kdiv(Attrs(id = AlDomID.documentCategoryPickerContainer))
                                    }
                                    o- kdiv{o->
                                        o- kbutton(Attrs(id = AlDomID.createOrderForm_submitButton, className = "btn btn-primary"), t("TOTE", "Вперед"))
                                        o- kdiv.id(AlDomID.ticker){}
                                    }
                                }
                            } else {
                                // TODO:vgrechka Think about making DB calls non-blocking
                                val order = alOrderRepo.save(newAlOrder(
                                    email = email, contactName = contactName, phone = phone,
                                    documentTitle = documentTitle, documentDetails = documentDetails))
                                o- renderOrderTitle(order)
                                o- kdiv.className(AlCSS.successBanner).text(t("TOTE", "Все круто. Мы с тобой скоро свяжемся"))
                                o- AlRenderPile.renderOrderParams(order)
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
                        ctx.exchange.requestReceiver.receiveFullString(onSuccess, onError, Charsets.UTF_8)
                    } else {
                        val data = when {
                            ctx.debug.messAroundBack201.should -> {
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

                private fun spitUsualPage(pageID: String, ctx: AlRequestContext, build: (Tag) -> Unit) {
                    ctx.shit.pageID = pageID
                    ctx.shit.postURL = "${AlBackPile0.baseURL}${AlBackPile0.orderCreationPagePath}"

                    // XXX ctx.shit is populated as part of `build`
                    val content = kdiv.id(AlDomID.replaceableContent){o->
                        o- kdiv.className("container", build)
                    }.render()

                    ctx.exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/html; charset=utf-8")
                    ctx.exchange.responseSender.send(buildString {
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
                        ln("        window['${ctx.shit::class.simpleName}'] = '${ObjectMapper().writeValueAsString(ctx.shit)}'")
                        ln("    </script>")
                        ln("</head>")
                        ln("<body>")
                        ln(AlSharedPile.beginContentMarker)
                        ln(content)
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
        File(AlBackPile0.tmpDirPath + "/alraune-back-started").writeText("Fuck, yeah...")
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

interface Should {
    val should: Boolean
}

class AlRequestContext {
    var exchange by volatileNotNullOnce<HttpServerExchange>()
    val shit = ShitPassedFromBackToFront()

    val debug = _Debug()
    inner class _Debug {
        val messAroundBack201 by fuck()


        fun fuck() = object : ReadOnlyProperty<Any?, Should> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Should {
                return object : Should {
                    override val should: Boolean
                        get() = exchange.queryParameters["backMessAround"]?.contains(property.name) == true
                }
            }
        }
    }
}





















