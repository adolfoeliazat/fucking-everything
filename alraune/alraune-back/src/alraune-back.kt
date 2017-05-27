package alraune.back

import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import alraune.back.AlBackPile0.log
import alraune.back.AlRenderPile.pageTitle
import alraune.back.AlRenderPile.rawHTML
import alraune.shared.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters

// TODO:vgrechka Backend dies on exception?

object StartAlrauneBack {
    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("user.timezone", "GMT")
        backPlatform.springctx = AnnotationConfigApplicationContext(AlrauneTestAppConfig::class.java)

        // val httpPort = 80
        val httpsPort = 443
        val port = httpsPort

        val server = Server()

        // val httpConnector = ServerConnector(server)
        // httpConnector.setPort(httpPort)

        val https = HttpConfiguration()
        https.addCustomizer(SecureRequestCustomizer())

        val sslContextFactory = SslContextFactory()
        sslContextFactory.keyStorePath = AlBackPile0.secrets.keyStore
        sslContextFactory.setKeyStorePassword(AlBackPile0.secrets.keyStorePassword)
        sslContextFactory.setKeyManagerPassword(AlBackPile0.secrets.keyManagerPassword)

        val httpsConnector = ServerConnector(server,
                                             SslConnectionFactory(sslContextFactory, "http/1.1"),
                                             HttpConnectionFactory(https))
        httpsConnector.port = httpsPort

        server.connectors = arrayOf(/*httpConnector,*/ httpsConnector)

        server.handler = object : HandlerCollection() {
            val servletHandler = ServletHandler()-{o->
                val servlet = object : HttpServlet() {
                    override fun service(req: HttpServletRequest, res: HttpServletResponse) {
                        AlRequestContext.the = AlRequestContext().also {
                            it.req = req
                            it.res = res

                            it.getParams = run {
                                if (it.isPost) {
                                    // Attempt to read request body after `getParameter` call causes exception, kind of because shit is already consumed.
                                    // In case of POST we are passing all data via body anyway, so...
                                    AlGetParams()
                                } else {
                                    val ctor = AlGetParams::class.constructors.first()
                                    ctor.call(*ctor.valueParameters.map {
                                        req.getParameter(it.name)
                                    }.toTypedArray())
                                }
                            }
                        }

                        fun jerk() {
                            shitToFront("c125ccc7-3bdc-499f-ab19-a12ecc826fa5") {shit->
                                val requestContextID = AlRequestContext.the.requestContextID
                                shit.requestContextID = requestContextID
                                AlBackDebug.idToRequestContext[requestContextID] = AlRequestContext.the

                                shit.baseURL = AlBackPile0.baseURL
                            }

                            log.debug("req.pathInfo = ${req.pathInfo}")
                            req.characterEncoding = "UTF-8"
                            res.contentType = "text/html; charset=utf-8"

                            when (req.pathInfo) {
                                "/alraune.css" -> {
                                    res.contentType = "text/css; charset=utf-8"
                                    res.writer.print(AlCSS_Back.sheet)
                                }
                            // TODO:vgrechka @unboilerplate
                                AlPagePath.debug_post_dumpStackByID -> handlePost_debug_post_dumpStackByID()
                                AlPagePath.debug_post_dumpBackCodePath -> handlePost_debug_post_dumpBackCodePath()
                                AlPagePath.debug_post_fuckDatabaseForNextPost -> handlePost_debug_post_fuckDatabaseForNextPost()
                                AlPagePath.orderCreationForm -> handleGet_orderCreationForm()
                                AlPagePath.post_createOrder -> handlePost_createOrder()
                                AlPagePath.orderParams -> handleGet_orderParams()
                                AlPagePath.post_setOrderParams -> handlePost_setOrderParams()
                                AlPagePath.orderFiles -> handleGet_orderFiles()
                                AlPagePath.post_addOrderFile -> handlePost_addOrderFile()
                                else -> {
                                    when {
                                        AlRequestContext.the.isPost -> bitch("pathInfo = ${req.pathInfo}    284bea9a-dc4f-4e62-8cc9-39508bb26c31")
                                        else -> spitLandingPage()
                                    }
                                }
                            }
                            res.status = HttpServletResponse.SC_OK
                        }

                        val needsTransaction = true
                        if (needsTransaction) {
                            backPlatform.tx {jerk()}
                        } else {
                            jerk()
                        }
                    }
                }
                o.addServletWithMapping(ServletHolder(servlet), "/*")
            }

            val backResourceHandler = ResourceHandler().also {
                it.resourceBase = AlBackPile0.backResourceRootDir
            }

            val frontResourceHandler = ResourceHandler().also {
                it.resourceBase = AlBackPile0.frontOutDirParent
            }

            val sharedKJSResourceHandler = ResourceHandler().also {
                it.resourceBase = AlBackPile0.sharedKJSOutDirParent
            }

            init {
                handlers = arrayOf(servletHandler, backResourceHandler, frontResourceHandler, sharedKJSResourceHandler)
            }

            override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
                if (isStarted) {
                    // log.debug("target = $target")
                    // log.debug("baseRequest.pathInfo = ${baseRequest.pathInfo}")
                    // log.debug("request.pathInfo = ${request.pathInfo}")

                    when {
                        target.startsWith("/node_modules/") -> backResourceHandler.handle(target, baseRequest, request, response)
                        target.startsWith("/alraune-front/") -> frontResourceHandler.handle(target, baseRequest, request, response)
                        target.startsWith("/shared-kjs/") -> sharedKJSResourceHandler.handle(target, baseRequest, request, response)
                        else -> servletHandler.handle(target, baseRequest, request, response)
                    }
                }
            }
        }

        server.start()

        log.info("Shit is spinning on port $port")
        File(AlBackPile0.tmpDirPath + "/alraune-back-started").writeText("Fuck, yeah...")
        server.join()
    }


}



object AlCSS_Back {
    val sheet by lazy {
        val buf = StringBuilder()
        AlCSS.addShit(buf)

        buf.append("""
            body {overflow-x: hidden; padding-right: 0px !important;}

            button:disabled {cursor: default !important;}
            input:disabled {cursor: default !important;}
            textarea:disabled {cursor: default !important;}
            select:disabled {cursor: default !important;}

            .form-control:focus {border-color: #b0bec5; box-shadow: inset 0 1px 1px rgba(0,0,0,.075),0 0 8px rgba(176,190,197,.6);}

            .btn-primary {background-color: #78909c; border-color: #546e7a;}
            .btn-primary:hover {background-color: #546e7a; border-color: #37474f;}
            .btn-primary:focus {background-color: #455a64; border-color: #263238; outline-color: #b0bec5;}
            .btn-primary:focus:hover {background-color: #455a64; border-color: #263238;}
            .btn-primary:active {background-color: #455a64; border-color: #263238;}
            .btn-primary:active:focus {background-color: #455a64; border-color: #263238; outline-color: #b0bec5;}
            .btn-primary:active:hover {background-color: #455a64; border-color: #263238;}

            .btn-primary.disabled.focus,
            .btn-primary.disabled:focus,
            .btn-primary.disabled:hover,
            .btn-primary[disabled].focus,
            .btn-primary[disabled]:focus,
            .btn-primary[disabled]:hover,
            fieldset[disabled] .btn-primary.focus,
            fieldset[disabled] .btn-primary:focus,
            fieldset[disabled] .btn-primary:hover {
                background-color: #78909c;
                border-color: #546e7a;
            }
        """)

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
    // TODO:vgrechka Introduce log specific to request, in addition to the global one in AlBackPile0
    val requestContextID = DebugPile.nextPUID().toString()
    var req by notNullOnce<HttpServletRequest>()
    var res by notNullOnce<HttpServletResponse>()
    val shitPassedFromBackToFront = PieceOfShitFromBack()
    var getParams by notNullOnce<AlGetParams>()
    val codeSteps = mutableListOf<CodeStep>()

    companion object {
        private val threadLocal = ThreadLocal<AlRequestContext>()

        var the
            get() = threadLocal.get()!!
            set(value) {threadLocal.set(value)}
    }

    val isPost get() = req.method == "POST"
}

fun spitLandingPage() {
    spitUsualPage(pageTitle("Fuck You"))
}

val shitForFront get() = AlRequestContext.the.shitPassedFromBackToFront

fun replaceableContent(content: Renderable) =
    insideMarkers(id = AlDomID.replaceableContent, content = content)

fun insideMarkers(id: String, content: Renderable? = null, tamperWithAttrs: (Attrs) -> Attrs = {it}): Tag {
    val beginMarker = AlSharedPile.beginContentMarkerForDOMID(id)
    val endMarker = AlSharedPile.endContentMarkerForDOMID(id)
    return kdiv{o->
        o- rawHTML(beginMarker)
        o- kdiv(tamperWithAttrs(Attrs(id = id))){o->
            o- content
        }
        o- rawHTML(endMarker)
    }
}

fun shitToFront(shitterUID: String, block: (PieceOfShitFromBack) -> Unit) {
    AlRequestContext.the.codeSteps += CodeStep(
        title = "shitToFront    $shitterUID",
        throwableForStack = Exception("Capturing stack"),
        stackStringLinesToDrop = 2)
    shitForFront.logOfShitters += shitterUID + "; "
    block(shitForFront)
}

fun <T : Any> readPostData(klass: KClass<T>): T {
    val dataString = AlRequestContext.the.req.reader.readText()
    return ObjectMapper().readValue(dataString, klass.java)
}




fun makeURLPart(path: String, params: AlGetParams = AlGetParams()): String {
    val buf = StringBuilder(path)
    var first = true
    for (p in AlGetParams::class.declaredMemberProperties) {
        p.get(params)?.let {value->
            if (first) {
                first = false
                buf += "?"
            }
            buf += "${p.name}=$value" // TODO:vgrechka Encode
        }
    }
    return buf.toString()
}


fun spitUsualPage(pipiska: Renderable) {
    val ctx = AlRequestContext.the
    val html = kdiv.className("container"){o->
        o- pipiska
        o- insideMarkers(AlDomID.shitPassedFromBackToFront, tamperWithAttrs = {
            it.copy(dataShit = ObjectMapper().writeValueAsString(ctx.shitPassedFromBackToFront))
        })
    }.render()

    ctx.res.writer.print(buildString {
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
        ln("</head>")
        ln("<body>")
        ln(html)
        ln("    <script src='/node_modules/jquery/dist/jquery.min.js'></script>")
        ln("    <script src='/node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
        ln("    <script src='/alraune-front/lib/kotlin.js'></script>")
        ln("    <script src='/shared-kjs/shared-kjs.js?${System.currentTimeMillis()}'></script>")
        ln("    <script src='/alraune-front/alraune-front.js?${System.currentTimeMillis()}'></script>")
        ln("</body>")
        ln("</html>")
    })
}

enum class AlDocumentType(val title: String) {
    ABSTRACT("Реферат"),
    COURSE("Курсовая работа"),
    GRADUATION("Дипломная работа"),
    LAB("Лабораторная работа"),
    TEST("Контрольная работа"),
    RGR("РГР"),
    DRAWING("Чертеж"),
    DISSERTATION("Диссертация"),
    ESSAY("Эссе (сочинение)"),
    PRACTICE("Отчет по практике"),
    OTHER("Другое")
}


class FuckingField(val value: String, val render: () -> Renderable)

class FieldContext {
    var hasErrors = false
}

fun declareField(ctx: FieldContext,
                 prop: KProperty0<String>,
                 title: String, validator: (String?) -> ValidationResult,
                 fieldType: FieldType = FieldType.TEXT): FuckingField {
    val vr = validator(prop.get())
    val theError = when {
        AlRequestContext.the.isPost -> vr.error
        else -> null
    }
    if (theError != null)
        ctx.hasErrors = true

    return FuckingField(
        value = vr.sanitizedString,
        render = {
            val id = AlSharedPile.fieldDOMID(name = prop.name)
            kdiv.className("form-group") {o->
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
    )
}

interface WithFieldContext {
    val fieldCtx: FieldContext
}

class OrderParamsFields(val data: OrderCreationFormPostData) : WithFieldContext {
    val f = AlFields.order
    val v = AlBackPile
    override val fieldCtx = FieldContext()

    val email = declareField(fieldCtx, data::email, f.email.title, v::validateEmail)
    val contactName = declareField(fieldCtx, data::name, f.contactName.title, v::validateName)
    val phone = declareField(fieldCtx, data::phone, f.phone.title, v::validatePhone)
    val documentTitle = declareField(fieldCtx, data::documentTitle, f.documentTitle.title, v::validateDocumentTitle)
    val documentDetails = declareField(fieldCtx, data::documentDetails, f.documentDetails.title, v::validateDocumentDetails, FieldType.TEXTAREA)
    val numPages = declareField(fieldCtx, data::numPages, f.numPages.title, v::validateNumPages)
    val numSources = declareField(fieldCtx, data::numSources, f.numSources.title, v::validateNumSources)
}

val rctx get() = AlRequestContext.the

object AlBackDebug {
    val idToRequestContext = ConcurrentHashMap<String, AlRequestContext>()

//    fun maybeMessAround() {
//        rctx.req.getDateHeader(-)
//    }
//
//    val debug = _Debug()
//    inner class _Debug {
//        val messAroundBack201 by fuck()
//
//        fun fuck() = object : ReadOnlyProperty<Any?, Should> {
//            override fun getValue(thisRef: Any?, property: KProperty<*>): Should {
//                return object : Should {
//                    override val should: Boolean
//                        get() = req.getParameter("mab")?.contains(property.name) == true
//                }
//            }
//        }
//    }
}

























