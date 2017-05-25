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
import alraune.back.AlRenderPile.col
import alraune.back.AlRenderPile.pageTitle
import alraune.back.AlRenderPile.renderOrderTitle
import alraune.back.AlRenderPile.row
import alraune.back.AlRenderPile.t
import alraune.shared.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters

// TODO:vgrechka Field: document type

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
                        val path_debug_dumpStackByID = "/debug_dumpStackByID"

                        AlRequestContext.the = AlRequestContext().also {
                            it.req = req
                            it.res = res

                            it.getParams = run {
                                if (req.method == "POST") {
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

                            it.shitPassedFromBackToFront.debug_urlForSendingStackID = makeURL(path_debug_dumpStackByID)
                        }

                        log.debug("req.pathInfo = ${req.pathInfo}")
                        req.characterEncoding = "UTF-8"
                        res.contentType = "text/html; charset=utf-8"

                        when (req.pathInfo) {
                            "/alraune.css" -> {
                                res.contentType = "text/css; charset=utf-8"
                                res.writer.print(AlCSS_Back.sheet)
                            }
                            path_debug_dumpStackByID -> {
                                val json = req.reader.readText()
                                val bean = ObjectMapper().readValue(json, DumpStackByIDRequest::class.java)
                                val stack = AlBackPile.idToTagCreationStack[bean.stackID] ?: bitch("5aaece41-c3f3-4eae-8c98-e7f69147ef3b")
                                clog(stack
                                         .lines()
                                         .filter {line-> !listOf(
                                             "Tag.<init>",
                                             "TagCtor.invoke")
                                             .any {line.contains(it)}}
                                         .joinToString("\n"))
                            }
                            AlPagePath.orderCreationForm -> spitOrderCreationFormPage()
                            AlPagePath.orderParams -> spitOrderParamsPage()
                            else -> spitLandingPage()
                        }
                        res.status = HttpServletResponse.SC_OK
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
    var req by notNullOnce<HttpServletRequest>()
    var res by notNullOnce<HttpServletResponse>()
    val shitPassedFromBackToFront = ShitPassedFromBackToFront()
    var getParams by notNullOnce<AlGetParams>()
    private var nextUID = 1
//    val scriptPile = StringBuilder()

    companion object {
        private val threadLocal = ThreadLocal<AlRequestContext>()

        var the
            get() = threadLocal.get()!!
            set(value) {threadLocal.set(value)}
    }

    fun nextUID() = nextUID++

    val debug = _Debug()
    inner class _Debug {
        val messAroundBack201 by fuck()

        fun fuck() = object : ReadOnlyProperty<Any?, Should> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Should {
                return object : Should {
                    override val should: Boolean
                        get() = req.getParameter("backMessAround")?.contains(property.name) == true
                }
            }
        }
    }
}

fun spitLandingPage() {
    spitUsualPage {o->
        o- pageTitle("Fuck You")
    }
}

fun spitOrderCreationFormPage() {
    val ctx = AlRequestContext.the
    val isPost = ctx.req.method == "POST"

    val data = when {
        isPost -> {
            val dataString = ctx.req.reader.readText()
            ObjectMapper().readValue(dataString, OrderCreationForm::class.java)
        }
        else -> when {
            ctx.debug.messAroundBack201.should -> {
                imf("79569177-b1c4-4692-9230-0ad7f3e75ae9")
//                                OrderCreationForm(email = "fuck",
//                                                  name = "shit",
//                                                  phone = "bitch",
//                                                  documentTitle = "boobs",
//                                                  documentDetails = "vagina")
            }
            else -> OrderCreationForm(
                email = "", name = "", phone = "", documentTypeID = AlDocumentType.ABSTRACT.name, documentTitle = "",
                documentDetails = "", documentCategoryID = AlDocumentCategories.miscID, numPages = "", numSources = "")
        }
    }


    spitUsualPage {o->
        val q = AlBackPile
        var hasErrors = false

        class FuckingField(val value: String, val render: () -> Renderable)

        fun declareField(prop: KProperty0<String>,
                         title: String, validator: (String?) -> ValidationResult,
                         fieldType: FieldType = FieldType.TEXT): FuckingField {
            val vr = validator(prop.get())
            val theError = when {
                isPost -> vr.error
                else -> null
            }
            if (theError != null)
                hasErrors = true

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

        // TODO:vgrechka @improve d0fc960d-76be-4a0b-969c-7bbf94275e09
        val f = AlFields.order
        val email = declareField(data::email, f.email.title, q::validateEmail)
        val contactName = declareField(data::name, f.contactName.title, q::validateName)
        val phone = declareField(data::phone, f.phone.title, q::validatePhone)
        val documentTitle = declareField(data::documentTitle, f.documentTitle.title, q::validateDocumentTitle)
        val documentDetails = declareField(data::documentDetails, f.documentDetails.title, q::validateDocumentDetails, FieldType.TEXTAREA)
        val numPages = declareField(data::numPages, f.numPages.title, q::validateNumPages)
        val numSources = declareField(data::numSources, f.numSources.title, q::validateNumSources)

        if (!isPost || hasErrors) {
            ctx.shitPassedFromBackToFront.pageID = AlPageID.orderCreationForm
            ctx.shitPassedFromBackToFront.postURL = makeURL(AlPagePath.orderCreationForm)

            ctx.shitPassedFromBackToFront.documentCategoryID = data.documentCategoryID
            o- pageTitle(t("TOTE", "Заказ"))
            o- kform{o->
                if (hasErrors)
                    o- kdiv.className(AlCSS.errorBanner).text(t("TOTE", "Кое-что нужно исправить..."))

                o- row(marginBottom = null){o->
                    o- col(4, contactName.render())
                    o- col(4, email.render())
                    o- col(4, phone.render())
                }
                o- row(marginBottom = null){o->
                    o- col(4, kdiv.className("form-group"){o->
                        o- klabel(text = f.documentType.title)
                        o- kselect(Attrs(id = AlSharedPile.fieldDOMID(name = OrderCreationForm::documentTypeID.name),
                                         className = "form-control")) {o->
                            for (value in AlDocumentType.values()) {
                                o- koption(Attrs(value = value.name,
                                                 selected = data.documentTypeID == value.name),
                                           value.title)
                            }
                        }
                    })
                    o- col(8, kdiv.className("form-group"){o->
                        o- klabel(text = f.documentCategory.title)
                        o- kdiv(Attrs(id = AlDomID.documentCategoryPickerContainer))
                    })
                }
                o- documentTitle.render()
                o- row(marginBottom = null){o->
                    o- col(6, numPages.render())
                    o- col(6, numSources.render())
                }
                o- documentDetails.render()
                o- kdiv(Attrs(id = AlDomID.filePickerContainer))
                o- kdiv{o->
                    o- kbutton(Attrs(id = AlDomID.createOrderForm_submitButton, className = "btn btn-primary"), t("TOTE", "Продолжить"))
                    o- kdiv.id(AlDomID.ticker){}
                }
            }
        } else {
            AlDocumentCategories.findByIDOrBitch(data.documentCategoryID)
            AlDocumentType.values().find {it.name == data.documentTypeID} ?: bitch("e63b006c-3cda-4db8-b7e0-e2413e980dbc")

            val order = alUAOrderRepo.save(newAlUAOrder(
                uuid = UUID.randomUUID().toString(), state = UAOrderState.CUSTOMER_DRAFT,
                email = email.value, contactName = contactName.value, phone = phone.value,
                documentTitle = documentTitle.value, documentDetails = documentDetails.value,
                documentTypeID = data.documentTypeID, documentCategoryID = data.documentCategoryID,
                numPages = numPages.value.toInt(), numSources = numSources.value.toInt()))

            ctx.shitPassedFromBackToFront.historyPushState = makeURL(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))
            nancyOrderParams(o, order)
        }
    }
}

private fun nancyOrderParams(o: Tag, order: AlUAOrder) {
    val ctx = AlRequestContext.the
    ctx.shitPassedFromBackToFront.pageID = AlPageID.orderParams

    o- renderOrderTitle(order)
//            o- kdiv.className(AlCSS.successBanner).text(t("TOTE", "Все круто, заказ создан. Мы с тобой скоро свяжемся"))
    o- kdiv.className(AlCSS.submitForReviewBanner){o->
        o- kdiv(Style(flexGrow = "1")).text(t("TOTE", "Убедись, что все верно. Подредактируй, если нужно. Возможно, добавь файлы. А затем..."))
        o- kbutton(Attrs(id = AlDomID.submitOrderForReviewButton, className = "btn btn-primary"), t("TOTE", "Отправить на проверку"))
    }
    o- AlRenderPile.renderOrderParams(order)
}

fun makeURL(path: String, params: AlGetParams = AlGetParams()): String {
    val buf = StringBuilder("${AlBackPile0.baseURL}$path")
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

fun spitOrderParamsPage() {
    val ctx = AlRequestContext.the
    val isPost = ctx.req.method == "POST"

//    val data = when {
//        isPost -> {
//            imf("bf0bb107-b3d7-4d41-b495-6912c7b7108c")
////            val dataString = ctx.req.reader.readText()
////            ObjectMapper().readValue(dataString, OrderCreationForm::class.java)
//        }
//        else -> OrderCreationForm(
//            email = "", name = "", phone = "", documentTypeID = AlDocumentType.ABSTRACT.name, documentTitle = "",
//            documentDetails = "", documentCategoryID = AlDocumentCategories.miscID, numPages = "", numSources = "")
//    }

    spitUsualPage {o->
        val uuid = ctx.getParams.orderUUID ?: bitch("0fe1dd78-8afd-4511-b743-7fc3b5ac78ce")
        val order = alUAOrderRepo.findByUuid(uuid) ?: bitch("bcfc6c38-585c-43f9-8984-c26d9c113e4e")
        nancyOrderParams(o, order)
    }
}

private fun spitUsualPage(build: (Tag) -> Unit) {
    val ctx = AlRequestContext.the
    val content = kdiv.id(AlDomID.replaceableContent){o->
        o- kdiv.className("container", build)
        o- kdiv(Attrs(id = ctx.shitPassedFromBackToFront::class.simpleName,
                      dataShit = ObjectMapper().writeValueAsString(ctx.shitPassedFromBackToFront)))
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


