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
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberFunctions
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
                            rctx.getParams.mab?.let {mab->
                                val f = AlBackDebug::class.memberFunctions.find {it.name == mab} ?: bitch("mab = $mab    2be4e39e-6e40-4930-a5ad-4b5340705727")
                                f.call(AlBackDebug)
                            }

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
                                    res.writer.print(AlCSS.sheet)
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

object AlCSS {
    data class Pack(
        val default: String? = null,
        val link: String? = null,
        val visited: String? = null,
        val hover: String? = null,
        val active: String? = null,
        val focus: String? = null,
        val hoverActive: String? = null,
        val hoverFocus: String? = null,
        val firstChild: String? = null,
        val notFirstChild: String? = null
    ) {
        var className by notNullOnce<String>()
    }


    object carla {
        val pizda = Pack("""
            background-color: ${Color.BLUE_300};
        """)

        val cunt = Pack("""
            background-color: ${Color.BLUE_500};
        """)
    }

    val errorBanner = Pack("""
        background-color: ${Color.RED_50};
        border-left: 3px solid ${Color.RED_300};
        margin-bottom: 1.5rem;
        padding-left: 1rem;
        padding-top: 1rem;
        padding-bottom: 1rem;""")

    val successBanner = Pack("""
        background-color: ${Color.GREEN_50};
        border-left: 3px solid ${Color.GREEN_300};
        margin-bottom: 1.5rem;
        padding-left: 1rem;
        padding-top: 1rem;
        padding-bottom: 1rem;""")

    val submitForReviewBanner = Pack("""
        background-color: #eceff1;
        border-left: 3px solid #90a4ae;
        margin-bottom: 1rem;
        padding-left: 1rem;
        padding-right: 0;
        padding-top: 0.5rem;
        padding-bottom: 0.5rem;
        display: flex;
        align-items: center;""")

    val sheet = run {
        val buf = StringBuilder()

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

            #${AlDomID.ticker} {
                display: none;
                width: 14px;
                background-color: ${Color.BLUE_GRAY_600};
                height: 34px;
                float: right;
                animation-name: ${AlDomID.ticker};
                animation-duration: 500ms;
                animation-iteration-count: infinite;
            }

            @keyframes ${AlDomID.ticker} {
                0% {
                    opacity: 1;
                }

                100% {
                    opacity: 0;
                }
            }
        """)

        fun fart(clazz: KClass<*>, selectorPrefix: String = "") {
            for (prop in clazz.memberProperties) {
                prop as KProperty1<Any?, Any?>
                if (prop.returnType.classifier == Pack::class) {
                    val pack = prop.get(clazz.objectInstance) as Pack
                    pack.className = selectorPrefix + prop.name
                    val selector = "." + pack.className
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

            for (nestedClass in clazz.nestedClasses) {
                if (nestedClass.objectInstance != null) {
                    fart(nestedClass, "${nestedClass.simpleName}-")
                }
            }
        }

        fart(AlCSS::class)

        buf.toString()
    }
}

class AlRequestContext {
    val requestContextID = DebugPile.nextPUID().toString()
    val log = AlBackPile0.log // TODO:vgrechka Make this log somehow specific to a particular request
    var req by notNullOnce<HttpServletRequest>()
    var res by notNullOnce<HttpServletResponse>()
    val shitPassedFromBackToFront = PieceOfShitFromBack()
    var getParams by notNullOnce<AlGetParams>() // TODO:vgrechka Rename
    val codeSteps = mutableListOf<CodeStep>()
    var createdOrder: AlUAOrder? = null

    val objectMapper by lazy {ObjectMapper().also {mapper->
        val typer = ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL)
            .init(JsonTypeInfo.Id.CLASS, null)
            .inclusion(JsonTypeInfo.As.PROPERTY)
            .typeProperty("@class")
        mapper.setDefaultTyping(typer)
        mapper.registerModule(SimpleModule().also {module->
            module.addDeserializer(KClass::class.java, object : StdDeserializer<KClass<*>>(null as Class<*>?) {
                override fun deserialize(jp: JsonParser, ctx: DeserializationContext): KClass<*> {
                    val node = jp.codec.readTree<JsonNode>(jp)
                    val simpleName = node.get("simpleName").asText()
                    return Class.forName("alraune.shared.$simpleName").kotlin
                }
            })
        })
    }}

    val postData by lazy {_postData()}; inner class _postData {
        val data = run {
            val text = req.reader.readText()
            objectMapper.readValue(text, Object::class.java)!!
        }
        val orderParams get() = data as OrderParamsFormPostData
        val orderFile get() = data as OrderFileFormPostData
        val dumpStackByID get() = data as DumpStackByIDPostData
        val dumpBackCodePath get() = data as DumpBackCodePathPostData
    }

    val orderUUID by lazy {
        if (isPost) {
            when (req.pathInfo) {
                AlPagePath.post_setOrderParams -> postData.orderParams.orderUUID ?: bitch("11034025-8877-4d96-a17f-f5c3c2f0e16d")
                AlPagePath.post_addOrderFile -> postData.orderFile.orderUUID
                else -> bitch("6f76e7f3-6f92-48d4-91ff-95f89f6626ce")
            }
        } else {
            getParams.orderUUID ?: bitch("7aa84c05-79c3-4c8f-bfd2-26250414305d")
        }
    }

    val order by lazy {
        createdOrder
        ?: alUAOrderRepo.findByUuid(orderUUID)
        ?: bitch("18755da5-0458-49a6-bfdc-9a4e611de4ed")
    }

    companion object {
        private val threadLocal = ThreadLocal<AlRequestContext>()

        var the
            get() = threadLocal.get()!!
            set(value) {threadLocal.set(value)}
    }

    val isPost get() = req.method == "POST"

    private fun <T : Any> readPostData(klass: KClass<T>): T {
        val dataString = AlRequestContext.the.req.reader.readText()
        return ObjectMapper().readValue(dataString, klass.java)
    }
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


interface FuckingField {
    val value: String
    fun validate()
    fun render(): Renderable
    fun noValidation()
}

class FieldContext {
    val fields = mutableListOf<FuckingField>()
    var hasErrors = false

    fun validate() {
        for (field in fields)
            field.validate()
    }

    fun noValidation() {
        for (field in fields)
            field.noValidation()
    }
}

fun declareField(ctx: FieldContext,
                 prop: KProperty0<String>,
                 title: String,
                 validator: (String?) -> ValidationResult,
                 fieldType: FieldType = FieldType.TEXT): FuckingField {
    val field = object : FuckingField {
        private var vr by notNullOnce<ValidationResult>()

        override val value get() = vr.sanitizedString

        override fun noValidation() {
            vr = ValidationResult(sanitizedString = prop.get(), error = null)
        }

        override fun validate() {
            fun noise(x: String) = AlRequestContext.the.log.debug(x)
            noise(::declareField.name + ": prop = ${prop.name}")

            vr = validator(prop.get())
            noise("    vr = $vr")
            val theError = when {
                AlRequestContext.the.isPost -> vr.error
                else -> null
            }
            if (theError != null)
                ctx.hasErrors = true
        }

        override fun render(): Renderable {
            val theError = vr.error
            val id = AlSharedPile.fieldDOMID(name = prop.name)
            return kdiv.className("form-group") {o ->
                if (theError != null)
                    o.amend(Style(marginBottom = "0"))
                o - klabel(text = title)
                val control = when (fieldType) {
                    FieldType.TEXT -> kinput(Attrs(type = "text", id = id, value = vr.sanitizedString, className = "form-control")) {}
                    FieldType.TEXTAREA -> ktextarea(Attrs(id = id, rows = 5, className = "form-control"), text = vr.sanitizedString)
                }
                o - kdiv(Style(position = "relative")) {o ->
                    o - control
                    if (theError != null) {
                        o - kdiv(Style(marginTop = "5px", marginRight = "9px", textAlign = "right", color = "${Color.RED_700}"))
                            .text(theError)
                        // TODO:vgrechka Shift red circle if control has scrollbar
                        o - kdiv(Style(width = "15px", height = "15px", backgroundColor = "${Color.RED_300}",
                                       borderRadius = "10px", position = "absolute", top = "10px", right = "8px"))
                    }
                }
            }
        }
    }
    ctx.fields += field
    return field
}

interface WithFieldContext {
    val fieldCtx: FieldContext
}

class OrderParamsFields(val data: OrderParamsFormPostData) : WithFieldContext {
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

    fun messAroundBack401() {
        val files = rctx.order.files
        files.clear()
        files.add(newAlUAOrderFile(uuid = AlBackPile.uuid(),
                                   state = UAOrderFileState.UNKNOWN,
                                   name = "lbxproxy.rtf",
                                   title = "Low Bandwidth X (LBX) proxy server configuration file",
                                   details = "Applications that would like to take advantage of the Low Bandwidth extension to X (LBX) must make their connections to an lbxproxy. These applications need know nothing about LBX, they simply connect to the lbxproxy as if it were a regular X server. The lbxproxy accepts client connections, multiplexes them over a single connection to the X server, and performs various optimizations on the X protocol to make it faster over low bandwidth and/or high latency connections. It should be noted that such compression will not increase the pace of rendering all that much. Its primary purpose is to reduce network load and thus increase overall network latency. A competing project called DXPC (Differential X Protocol Compression) has been found to be more efficient at this task. Studies have shown though that in almost all cases ssh tunneling of X will produce far better results than through any of these specialised pieces of software.",
                                   order = rctx.order))
        files.add(newAlUAOrderFile(uuid = AlBackPile.uuid(),
                                   state = UAOrderFileState.UNKNOWN,
                                   name = "xdm.rtf",
                                   title = "X display manager",
                                   details = "Manages a collection of X servers, which may be on the local host or remote machines. It provides services similar to those provided by init, getty, and login on character-based terminals: prompting for login name and password, authenticating the user, and running a session. xdm supports XDMCP (X Display Manager Control Protocol) and can also be used to run a chooser process which presents the user with a menu of possible hosts that offer XDMCP display management. If the xutils package is installed, xdm can use the sessreg utility to register login sessions to the system utmp file; this, however, is not necessary for xdm to function.",
                                   order = rctx.order))
        files.add(newAlUAOrderFile(uuid = AlBackPile.uuid(),
                                   state = UAOrderFileState.UNKNOWN,
                                   name = "gdm.rtf",
                                   title = "GNOME Display Manager",
                                   details = "Provides the equivalent of a \"login:\" prompt for X displays- it pops up a login window and starts an X session. It provides all the functionality of xdm, including XDMCP support for managing remote displays. The greeting window is written using the GNOME libraries and hence looks like a GNOME application- even to the extent of supporting themes! By default, the greeter is run as an unprivileged user for security.",
                                   order = rctx.order))
        files.add(newAlUAOrderFile(uuid = AlBackPile.uuid(),
                                   state = UAOrderFileState.UNKNOWN,
                                   name = "lilo.conf.rtf",
                                   title = "Configuration file for the Linux boot loader",
                                   details = "LILO is the original OS loader and can load Linux and others. The 'lilo' package normally contains lilo (the installer) and boot-record-images to install Linux, OS/2, DOS and generic Boot Sectors of other Oses. You can use Lilo to manage your Master Boot Record (with a simple text screen, text menu or colorful splash graphics) or call 'lilo' from other boot-loaders to jump-start the Linux kernel. ",
                                   order = rctx.order))
    }
}

























