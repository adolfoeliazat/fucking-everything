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
import alraune.back.AlRenderPile.rawHTML
import alraune.shared.*
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

// TODO:vgrechka Backend dies on exception?

object StartAlrauneBack {
    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("user.timezone", "GMT")
//        TSPile.spitOutSomeTS()
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

//                            it.getParams = run {
//                                if (it.isPost) {
//                                    // Attempt to read request body after `getParameter` call causes exception, kind of because shit is already consumed.
//                                    // In case of POST we are passing all data via body anyway, so...
//                                    AlGetParams()
//                                } else {
//                                    val ctor = AlGetParams::class.constructors.first()
//                                    ctor.call(*ctor.valueParameters.map {
//                                        req.getParameter(it.name)
//                                    }.toTypedArray())
//                                }
//                            }
                        }

                        fun jerk() {
//                            rctx.getParams.mab?.let {mab->
//                                val f = AlBackDebug::class.memberFunctions.find {it.name == mab} ?: bitch("mab = $mab    2be4e39e-6e40-4930-a5ad-4b5340705727")
//                                f.call(AlBackDebug)
//                            }

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
                                "/favicon.ico" -> {
                                    // TODO:vgrechka ...
                                    res.contentType = "text/css; charset=utf-8"
                                    res.writer.print("")
                                }

                                "/alraune.css" -> {
                                    res.contentType = "text/css; charset=utf-8"
                                    res.writer.print(AlCSS.sheet)
                                }

                                "/alraune.js" -> {
                                    res.contentType = "application/javascript; charset=utf-8"
                                    res.writer.print(hackTSJS())
                                }

                                "/alraune.js.map" -> {
                                    res.contentType = "text/plain; charset=utf-8"
                                    res.writer.print(File(AlBackPile0.tsMapOutputFile).readText())
                                }

                                "/fuckingCall" -> {
                                    handleFuckingCall()
                                }

                                AlPagePath.orderCreationForm -> spitOrderCreationFormPage()
                                AlPagePath.order -> spitOrderPage()

                                else -> {
                                    when {
//                                        AlRequestContext.the.isPost -> bitch("pathInfo = ${req.pathInfo}    284bea9a-dc4f-4e62-8cc9-39508bb26c31")
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
                        target.startsWith("/node_modules/") || target.startsWith("/ts/out/") -> backResourceHandler.handle(target, baseRequest, request, response)
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

class PieceOfShitFromBack2 {
    val commands = mutableListOf<AlBackToFrontCommandPile>()
}



class AlRequestContext {
    val requestContextID = DebugPile.nextPUID().toString()
    val log = AlBackPile0.log // TODO:vgrechka Make this log somehow specific to a particular request
    var req by notNullOnce<HttpServletRequest>()
    var res by notNullOnce<HttpServletResponse>()
    val shitPassedFromBackToFront = PieceOfShitFromBack()
    val shitPassedFromBackToFront2 = PieceOfShitFromBack2()
    val codeSteps = mutableListOf<CodeStep>()

    val ftb by lazy {
        val requestText = rctx.req.reader.readText()
        val ftb = ObjectMapper().readValue(requestText, AlFrontToBackCommandPile::class.java)!!
        clog("ftb", ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ftb))
        ftb
    }

    val getParams by lazy {
        val ctor = AlGetParams::class.primaryConstructor!!
        val xs = ctor.parameters.map {req.getParameter(it.name)}
        ctor.call(*xs.toTypedArray())
    }

    val maybeOrderUUID by lazy {when{
        isPost -> ftb.orderUUID
        else -> getParams.orderUUID
    }}

    val orderUUID by lazy {maybeOrderUUID ?: bitch("428fa414-e891-4ece-a224-fb7e16b03f88")}

    val order by lazy {alUAOrderRepo.findByUuid(orderUUID) ?: bitch("d65541de-d1cc-41b7-93b2-6f741c5a74eb")}


    companion object {
        private val threadLocal = ThreadLocal<AlRequestContext>()

        var the
            get() = threadLocal.get()!!
            set(value) {threadLocal.set(value)}
    }

    val isPost get() = req.method == "POST"
}

fun spitLandingPage() {
    imf("8ca6cab6-bc76-4485-b267-bfc0794693af")
//    spitUsualPage(pageTitle("Fuck You"))
}

val shitForFront get() = AlRequestContext.the.shitPassedFromBackToFront
val shitForFront2 get() = AlRequestContext.the.shitPassedFromBackToFront2

fun replaceableContent(content: Renderable) =
    insideMarkers(id = AlDomid.replaceableContent, content = content)

fun insideMarkers(id: AlDomid, content: Renderable? = null, tamperWithAttrs: (Attrs) -> Attrs = {it}): Tag {
    val beginMarker = AlSharedPile_killme.beginContentMarkerForDOMID(id)
    val endMarker = AlSharedPile_killme.endContentMarkerForDOMID(id)
    return kdiv{o->
        o- rawHTML(beginMarker)
        o- kdiv(tamperWithAttrs(Attrs(domid = id))){o->
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

fun shitToFront2(shitterUID: String, block: (PieceOfShitFromBack2) -> Unit) {
    AlRequestContext.the.codeSteps += CodeStep(
        title = "shitToFront2    $shitterUID",
        throwableForStack = Exception("Capturing stack"),
        stackStringLinesToDrop = 2)
    shitForFront.logOfShitters += shitterUID + "; "
    block(shitForFront2)
}





fun makeURLPart(path: String, params: AlGetParams = AlGetParams()): String {
    val buf = StringBuilder(path)
    var first = true
    for (p in AlGetParams::class.declaredMemberProperties) {
        p.get(params)?.let {value->
            if (first) {
                first = false
                buf += "?"
            } else {
                buf += "&"
            }
            buf += "${p.name}=$value" // TODO:vgrechka Encode
        }
    }
    return buf.toString()
}


//fun spitUsualPage(pipiska: Renderable) {
//    val ctx = AlRequestContext.the
//    val html = kdiv.className("container"){o->
//        o- pipiska
//
//        val om = ObjectMapper()
//        o- insideMarkers(AlDomid.shitPassedFromBackToFront, tamperWithAttrs = {
//            it.copy(dataShit = om.writeValueAsString(ctx.shitPassedFromBackToFront))
//        })
//        o- insideMarkers(AlDomid.shitPassedFromBackToFront2, tamperWithAttrs = {
//            it.copy(dataShit = om.writeValueAsString(ctx.shitPassedFromBackToFront2))
//        })
//    }.render()
//
//    ctx.res.writer.print(buildString {
//        ln("<!DOCTYPE html>")
//        ln("<html lang='en'>")
//        ln("<head>")
//        ln("    <meta charset='utf-8'>")
//        ln("    <meta http-equiv='X-UA-Compatible' content='IE=edge'>")
//        ln("    <meta name='viewport' content='width=device-width, initial-scale=1'>")
//        ln("    <title>Alraune</title>")
//        ln("")
//        ln("    <link href='/node_modules/bootstrap/dist/css/bootstrap.min.css' rel='stylesheet'>")
//        ln("    <link rel='stylesheet' href='/node_modules/font-awesome/css/font-awesome.min.css'>")
//        ln("    <link rel='stylesheet' href='alraune.css'>")
//        ln("</head>")
//        ln("<body>")
//        ln(html)
//        ln("    <script src='/node_modules/jquery/dist/jquery.min.js'></script>")
//        ln("    <script src='/node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
//        ln("    <script src='/alraune.js?${System.currentTimeMillis()}'></script>")
////        ln("    <script src='/ts/out/alraune.js?${System.currentTimeMillis()}'></script>")
//        // ln("    <script src='/alraune-front/lib/kotlin.js'></script>")
//        // ln("    <script src='/shared-kjs/shared-kjs.js?${System.currentTimeMillis()}'></script>")
//        // ln("    <script src='/alraune-front/alraune-front.js?${System.currentTimeMillis()}'></script>")
//        ln("</body>")
//        ln("</html>")
//    })
//}

fun spitUsualPage(pipiska: Renderable, initialBackResponse: AlBackResponsePile) {
    val ctx = AlRequestContext.the
    val html = pipiska.render()

    val json = ObjectMapper().writeValueAsString(initialBackResponse)

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
        ln("    <script src='/alraune.js?${System.currentTimeMillis()}'></script>")
        ln("    <script>window.initialBackResponse = $json</script>")
//        ln("    <script src='/ts/out/alraune.js?${System.currentTimeMillis()}'></script>")
        // ln("    <script src='/alraune-front/lib/kotlin.js'></script>")
        // ln("    <script src='/shared-kjs/shared-kjs.js?${System.currentTimeMillis()}'></script>")
        // ln("    <script src='/alraune-front/alraune-front.js?${System.currentTimeMillis()}'></script>")
        ln("</body>")
        ln("</html>")
    })
}

enum class AlUADocumentType(val title: String) {
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

//fun declareField(ctx: FieldContext,
//                 prop: KProperty0<String>,
//                 title: String,
//                 validator: (String?) -> ValidationResult,
//                 fieldType: FieldType = FieldType.TEXT): FuckingField {
//    val field = object : FuckingField {
//        private var vr by notNullOnce<ValidationResult>()
//
//        override val value get() = vr.sanitizedString
//
//        override fun noValidation() {
//            vr = ValidationResult(sanitizedString = prop.get(), error = null)
//        }
//
////        override fun validate() {
////            fun noise(x: String) = AlRequestContext.the.log.debug(x)
////            noise(::declareField.name + ": prop = ${prop.name}")
////
////            vr = validator(prop.get())
////            noise("    vr = $vr")
////            val theError = when {
////                AlRequestContext.the.isPost -> vr.error
////                else -> null
////            }
////            if (theError != null)
////                ctx.hasErrors = true
////        }
//
//        override fun render(): Renderable {
//            val theError = vr.error
//            val id = AlSharedPile.fieldDOMID(name = prop.name)
//            return kdiv.className("form-group"){o->
//                if (theError != null)
//                    o.amend(Style(marginBottom = "0"))
//                o - klabel(text = title)
//                val control = when (fieldType) {
//                    FieldType.TEXT -> kinput(Attrs(type = "text", id = id, value = vr.sanitizedString, className = "form-control")) {}
//                    FieldType.TEXTAREA -> ktextarea(Attrs(id = id, rows = 5, className = "form-control"), text = vr.sanitizedString)
//                }
//                o - kdiv(Style(position = "relative")){o->
//                    o - control
//                    if (theError != null) {
//                        o - kdiv(Style(marginTop = "5px", marginRight = "9px", textAlign = "right", color = "${Color.RED_700}"))
//                            .text(theError)
//                        // TODO:vgrechka Shift red circle if control has scrollbar
//                        o - kdiv(Style(width = "15px", height = "15px", backgroundColor = "${Color.RED_300}",
//                                       borderRadius = "10px", position = "absolute", top = "10px", right = "8px"))
//                    }
//                }
//            }
//        }
//    }
//    ctx.fields += field
//    return field
//}

interface WithFieldContext {
    val fieldCtx: FieldContext
}

//class OrderParamsFields(val data: OrderParamsFormPostData) : WithFieldContext {
//    val f = AlFields.order
//    val v = AlBackPile
//    override val fieldCtx = FieldContext()
//
//    val email = declareField(fieldCtx, data::email, f.email.title, v::validateEmail)
//    val contactName = declareField(fieldCtx, data::name, f.contactName.title, v::validateName)
//    val phone = declareField(fieldCtx, data::phone, f.phone.title, v::validatePhone)
//    val documentTitle = declareField(fieldCtx, data::documentTitle, f.documentTitle.title, v::validateDocumentTitle)
//    val documentDetails = declareField(fieldCtx, data::documentDetails, f.documentDetails.title, v::validateDocumentDetails, FieldType.TEXTAREA)
//    val numPages = declareField(fieldCtx, data::numPages, f.numPages.title, v::validateNumPages)
//    val numSources = declareField(fieldCtx, data::numSources, f.numSources.title, v::validateNumSources)
//}

val rctx get() = AlRequestContext.the

object AlBackDebug {
    val idToRequestContext = ConcurrentHashMap<String, AlRequestContext>()

//    fun messAroundBack401() {
//        val files = rctx.order.files
//        files.clear()
//
//        fun jerk(file: AlUAOrderFile) {
//            files.add(file)
//            alUAOrderFileRepo.save(file) // Save now to create ID
//        }
//
//        jerk(newAlUAOrderFile(uuid = "f1cdbb11-f7a0-4a63-9c63-12920df5bfee",
//                              state = UAOrderFileState.UNKNOWN,
//                              name = "lbxproxy.rtf",
//                              title = "Low Bandwidth X (LBX) proxy server configuration file",
//                              details = "Applications that would like to take advantage of the Low Bandwidth extension to X (LBX) must make their connections to an lbxproxy. These applications need know nothing about LBX, they simply connect to the lbxproxy as if it were a regular X server. The lbxproxy accepts client connections, multiplexes them over a single connection to the X server, and performs various optimizations on the X protocol to make it faster over low bandwidth and/or high latency connections. It should be noted that such compression will not increase the pace of rendering all that much. Its primary purpose is to reduce network load and thus increase overall network latency. A competing project called DXPC (Differential X Protocol Compression) has been found to be more efficient at this task. Studies have shown though that in almost all cases ssh tunneling of X will produce far better results than through any of these specialised pieces of software.",
//                              order = rctx.order))
//        jerk(newAlUAOrderFile(uuid = "9968705b-8879-46b1-99b9-26da1429501a",
//                                   state = UAOrderFileState.UNKNOWN,
//                                   name = "xdm.rtf",
//                                   title = "X display manager" + ". I am very long title".repeat(10),
//                                   details = "Manages a collection of X servers, which may be on the local host or remote machines. It provides services similar to those provided by init, getty, and login on character-based terminals: prompting for login name and password, authenticating the user, and running a session. xdm supports XDMCP (X Display Manager Control Protocol) and can also be used to run a chooser process which presents the user with a menu of possible hosts that offer XDMCP display management. If the xutils package is installed, xdm can use the sessreg utility to register login sessions to the system utmp file; this, however, is not necessary for xdm to function.",
//                                   order = rctx.order))
//        jerk(newAlUAOrderFile(uuid = "b169d1b4-8b0f-4ace-a5cb-f765e46fb9a6",
//                                   state = UAOrderFileState.UNKNOWN,
//                                   name = "gdm.rtf",
//                                   title = "GNOME Display Manager",
//                                   details = "Provides the equivalent of a \"login:\" prompt for X displays- it pops up a login window and starts an X session. It provides all the functionality of xdm, including XDMCP support for managing remote displays. The greeting window is written using the GNOME libraries and hence looks like a GNOME application- even to the extent of supporting themes! By default, the greeter is run as an unprivileged user for security.",
//                                   order = rctx.order))
//        jerk(newAlUAOrderFile(uuid = "9b3e9d1d-cddc-40aa-a785-b62e8020e983",
//                                   state = UAOrderFileState.UNKNOWN,
//                                   name = "lilo.conf.rtf",
//                                   title = "Configuration file for the Linux boot loader",
//                                   details = "LILO is the original OS loader and can load Linux and others. The 'lilo' package normally contains lilo (the installer) and boot-record-images to install Linux, OS/2, DOS and generic Boot Sectors of other Oses. You can use Lilo to manage your Master Boot Record (with a simple text screen, text menu or colorful splash graphics) or call 'lilo' from other boot-loaders to jump-start the Linux kernel. ",
//                                   order = rctx.order))
//    }
}

class PropertyNameSerializer : StdSerializer<KProperty1<*, *>>(KProperty1::class.java, true) {
    override fun serialize(value: KProperty1<*, *>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.name)
    }
}

fun spitOrderCreationFormPage() {
    val commands = mutableListOf<AlBackToFrontCommandPile>()
    emitCommandsForRenderingOrderCreationFormPage(commands, OrderParamsFields(FieldSource.INITIAL))
    val initialBackResponse = AlBackResponsePile()-{o->
        o.commands = commands
    }
    spitUsualPage(kdiv(Attrs(domid = AlDomid.replaceableContent)), initialBackResponse)
}























