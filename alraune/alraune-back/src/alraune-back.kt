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
import alraune.back.AlRenderPile.rawHTML
import alraune.back.AlRenderPile.renderOrderTitle
import alraune.back.AlRenderPile.row
import alraune.back.AlRenderPile.t
import alraune.shared.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import java.util.*
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
                            // TODO:vgrechka Unduplicate
                            AlPagePath.debug_post_dumpStackByID -> handlePost_debug_post_dumpStackByID()
                            AlPagePath.debug_post_dumpBackCodePath -> handlePost_debug_post_dumpBackCodePath()
                            AlPagePath.orderCreationForm -> handleGet_orderCreationForm()
                            AlPagePath.post_createOrder -> handlePost_createOrder()
                            AlPagePath.orderParams -> handleGet_orderParams()
                            AlPagePath.post_setOrderParams -> handlePost_setOrderParams()
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

private fun handlePost_debug_post_dumpStackByID() {
    val data = readPostData(DumpStackByIDPostData::class)
    val stack = AlBackPile.idToTagCreationStack[data.stackID] ?: bitch("5aaece41-c3f3-4eae-8c98-e7f69147ef3b")
    clog(stack
             .lines()
             .filter {line ->
                 !listOf(
                     "Tag.<init>",
                     "TagCtor.invoke")
                     .any {line.contains(it)}
             }
             .joinToString("\n"))
}

class CodeStep(val title: String, val throwableForStack: Throwable, val stackStringLinesToDrop: Int)

private fun handlePost_debug_post_dumpBackCodePath() {
    val data = readPostData(DumpBackCodePathPostData::class)
    clog("\n=============== requestContextID = ${data.requestContextID} ===================")
    val ctx = AlBackDebug.idToRequestContext[data.requestContextID] ?: bitch("data.requestID = ${data.requestContextID}    225159bd-f456-4cb2-9503-b8e6be6d6139")
    for ((index, codeStep) in ctx.codeSteps.withIndex()) {
        clog()
        clog("${index + 1}) ${codeStep.title}")
        clog(codeStep.throwableForStack.stackTraceString
                 .lines()
                 .drop(codeStep.stackStringLinesToDrop)
                 .map {"    $it"}
                 .joinToString("\n"))
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
    spitUsualPage(pageTitle("Fuck You"))
}

fun handleGet_orderCreationForm() {
    spitOrderCreationFormPage(
        OrderParamsFields(
            newAlUAOrder(
                uuid = "", state = UAOrderState.CUSTOMER_DRAFT, email = "",
                contactName = "", phone = "", documentTypeID = AlDocumentType.ABSTRACT.name, documentTitle = "",
                documentDetails = "", documentCategoryID = AlDocumentCategories.miscID, numPages = -1, numSources = -1)
                .toForm()))
}

val shitForFront get() = AlRequestContext.the.shitPassedFromBackToFront

fun replaceableContent(content: Renderable) =
    insideMarkers(id = AlDomID.replaceableContent,
                  beginMarker = AlSharedPile.beginContentMarker,
                  endMarker = AlSharedPile.endContentMarker,
                  content = content)

private fun insideMarkers(id: String, beginMarker: String, endMarker: String, content: Renderable): Tag {
    return kdiv{o->
        o- rawHTML(beginMarker)
        o- kdiv.id(id){o->
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

private fun spitOrderCreationFormPage(fields: OrderParamsFields) {
    shitToFront("cc6b96c4-d89b-41e9-b4db-85c6d985366e") {
        it.pageID = AlPageID.orderCreationForm
        it.postPath = makeURLPart(AlPagePath.post_createOrder)
    }
    spitUsualPage(replaceableContent(
        kdiv()
            .add(pageTitle(t("TOTE", "Заказ")))
            .add(renderOrderParamsForm(fields))))
}

private fun <T : Any> readPostData(klass: KClass<T>): T {
    val dataString = AlRequestContext.the.req.reader.readText()
    return ObjectMapper().readValue(dataString, klass.java)
}

fun renderOrderParamsForm(fields: OrderParamsFields): Renderable {
    val f = AlFields.order
    AlRequestContext.the.shitPassedFromBackToFront.documentCategoryID = fields.data.documentCategoryID
    return kform{o->
        if (fields.dfctx.hasErrors)
            o- kdiv.className(AlCSS.errorBanner).text(t("TOTE", "Кое-что нужно исправить..."))

        o- row(marginBottom = null){o->
            o- col(4, fields.contactName.render())
            o- col(4, fields.email.render())
            o- col(4, fields.phone.render())
        }
        o- row(marginBottom = null){o->
            o- col(4, kdiv.className("form-group"){o->
                o- klabel(text = f.documentType.title)
                o- kselect(Attrs(id = AlSharedPile.fieldDOMID(name = OrderCreationFormPostData::documentTypeID.name),
                                 className = "form-control")) {o->
                    for (value in AlDocumentType.values()) {
                        o- koption(Attrs(value = value.name,
                                         selected = fields.data.documentTypeID == value.name),
                                   value.title)
                    }
                }
            })
            o- col(8, kdiv.className("form-group"){o->
                o- klabel(text = f.documentCategory.title)
                o- kdiv(Attrs(id = AlDomID.documentCategoryPickerContainer))
            })
        }
        o- fields.documentTitle.render()
        o- row(marginBottom = null){o->
            o- col(6, fields.numPages.render())
            o- col(6, fields.numSources.render())
        }
        o- fields.documentDetails.render()
        o- kdiv(Attrs(id = AlDomID.filePickerContainer))
        o- kdiv{o->
            o- kbutton(Attrs(id = AlDomID.createOrderForm_submitButton, className = "btn btn-primary"), t("TOTE", "Продолжить"))
            o- kdiv.id(AlDomID.ticker){}
        }
    }
}

fun shitBigReplacementToFront(shitterUID: String) {
    shitToFront(shitterUID) {
        it.replacement_id = AlDomID.replaceableContent
        it.replacement_beginMarker = AlSharedPile.beginContentMarker
        it.replacement_endMarker = AlSharedPile.endContentMarker
    }
}

private fun spitOrderParamsPage(order: AlUAOrder, fields: OrderParamsFields) {
    shitToFront("054bb78d-238e-4313-9b75-820c5a37097c") {
        it.pageID = AlPageID.orderParams
        it.postPath = makeURLPart(AlPagePath.post_setOrderParams)
        it.orderUUID = order.uuid
    }

    spitUsualPage(replaceableContent(kdiv{o->
        val canEdit = order.state == UAOrderState.CUSTOMER_DRAFT

        o- renderOrderTitle(order)
//            o- kdiv.className(AlCSS.successBanner).text(t("TOTE", "Все круто, заказ создан. Мы с тобой скоро свяжемся"))
        o- kdiv.className(AlCSS.submitForReviewBanner){o->
            o- kdiv(Style(flexGrow = "1")).text(t("TOTE", "Убедись, что все верно. Подредактируй, если нужно. Возможно, добавь файлы. А затем..."))
            o- kbutton(Attrs(id = AlDomID.submitOrderForReviewButton, className = "btn btn-primary"), t("TOTE", "Отправить на проверку"))
        }

        o- kdiv(Style(position = "relative")){o->
            o- kdiv(Attrs(className = "nav nav-tabs", style = Style(marginBottom = "0.5rem"))){o->
                o- kli.className("active")
                    .add(ka(Attrs(href = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))))
                             .add(t("Parameters", "Параметры")))

                o- kli.className("")
                    .add(ka(Attrs(href = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))))
                             .add(t("Files", "Файлы")))
            }

            if (canEdit) {
                o- kbutton(Attrs(id = AlDomID.editOrderParamsButton, className = "btn btn-default",
                                 style = Style(position = "absolute", right = "0", top = "0")))
                    .add(ki.className(fa.pencil))
            }
        }

        o- AlRenderPile.renderOrderParams(order)

        if (canEdit) {
            o- AlRenderPile.renderModal(ModalParams(
                width = "80rem",
                leftMarginColor = Color.BLUE_GRAY_300,
                title = t("Parameters", "Параметры"),
                body = insideMarkers(id = AlDomID.modalContent,
                                     beginMarker = AlSharedPile.beginModalContentMarker,
                                     endMarker = AlSharedPile.endModalContentMarker,
                                     content = renderOrderParamsForm(fields))
            ))
        }
    }))
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

fun handleGet_orderParams() {
    val uuid = AlRequestContext.the.getParams.orderUUID ?: bitch("0fe1dd78-8afd-4511-b743-7fc3b5ac78ce")
    val order = alUAOrderRepo.findByUuid(uuid) ?: bitch("bcfc6c38-585c-43f9-8984-c26d9c113e4e")
    val fields = OrderParamsFields(order.toForm())
    shitBigReplacementToFront("37636e9d-5060-43b8-a50d-34a95fe5bce1")
    shitToFront("954a5058-5ae6-40c7-bb45-06b0eeae8bc7") {
        it.hasErrors = false
    }
    spitOrderParamsPage(order, fields)
}

private fun spitUsualPage(pipiska: Renderable) {
    val ctx = AlRequestContext.the
    val html = kdiv.className("container"){o->
        o- pipiska
        o- rawHTML(AlSharedPile.beginShitPassedFromBackToFrontMarker)
        o- kdiv(Attrs(id = AlDomID.shitPassedFromBackToFront,
                      dataShit = ObjectMapper().writeValueAsString(ctx.shitPassedFromBackToFront)))
        o- rawHTML(AlSharedPile.endShitPassedFromBackToFrontMarker)
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

class DeclareFieldContext {
    var hasErrors = false
}

fun declareField(ctx: DeclareFieldContext,
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


class OrderParamsFields(val data: OrderCreationFormPostData) {
    val f = AlFields.order
    val v = AlBackPile
    val dfctx = DeclareFieldContext()

    val email = declareField(dfctx, data::email, f.email.title, v::validateEmail)
    val contactName = declareField(dfctx, data::name, f.contactName.title, v::validateName)
    val phone = declareField(dfctx, data::phone, f.phone.title, v::validatePhone)
    val documentTitle = declareField(dfctx, data::documentTitle, f.documentTitle.title, v::validateDocumentTitle)
    val documentDetails = declareField(dfctx, data::documentDetails, f.documentDetails.title, v::validateDocumentDetails, FieldType.TEXTAREA)
    val numPages = declareField(dfctx, data::numPages, f.numPages.title, v::validateNumPages)
    val numSources = declareField(dfctx, data::numSources, f.numSources.title, v::validateNumSources)
}


private fun handlePost_createOrder() {
    val fields = OrderParamsFields(readPostData(OrderCreationFormPostData::class))
    shitBigReplacementToFront("d2039b9e-7c7e-4487-b230-78203c35fdf7")
    if (fields.dfctx.hasErrors) {
        spitOrderCreationFormPage(fields)
    } else {
        validateOrderParamsFields(fields)
        val order = alUAOrderRepo.save(newAlUAOrder(
            uuid = UUID.randomUUID().toString(), state = UAOrderState.CUSTOMER_DRAFT,
            email = fields.email.value, contactName = fields.contactName.value, phone = fields.phone.value,
            documentTitle = fields.documentTitle.value, documentDetails = fields.documentDetails.value,
            documentTypeID = fields.data.documentTypeID, documentCategoryID = fields.data.documentCategoryID,
            numPages = fields.numPages.value.toInt(), numSources = fields.numSources.value.toInt()))

        shitToFront("cce77e9c-e7f2-4f17-9554-0e27ee982ed2") {
            it.hasErrors = false
            it.historyPushState = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))
        }
        spitOrderParamsPage(order, fields)
    }
}

private fun validateOrderParamsFields(fields: OrderParamsFields) {
    AlDocumentCategories.findByIDOrBitch(fields.data.documentCategoryID)
    AlDocumentType.values().find {it.name == fields.data.documentTypeID} ?: bitch("e63b006c-3cda-4db8-b7e0-e2413e980dbc")
}

private fun handlePost_setOrderParams() {
    shitToFront("b4e2fd47-3a65-41a2-be93-959118883938") {
        it.hasErrors = true
    }
    val data = readPostData(OrderCreationFormPostData::class)
    val uuid = data.orderUUID ?: bitch("4c7f82b3-6347-4f25-8949-2f96e5af4713")
    val order = alUAOrderRepo.findByUuid(uuid) ?: bitch("0ef3a079-e1c6-41bf-bfa9-8540ae9d0082")
    val fields = OrderParamsFields(data)
    if (fields.dfctx.hasErrors) {
        shitToFront("030a3b7c-7f4d-4d69-8473-88396049630f") {
            it.replacement_id = AlDomID.modalContent
            it.replacement_beginMarker = AlSharedPile.beginModalContentMarker
            it.replacement_endMarker = AlSharedPile.endModalContentMarker
        }
        spitOrderParamsPage(order, fields)
    } else {
        validateOrderParamsFields(fields)

        order.email = fields.email.value
        order.contactName = fields.contactName.value
        order.phone = fields.phone.value
        order.documentTitle = fields.documentTitle.value
        order.documentDetails = fields.documentDetails.value
        order.documentTypeID = fields.data.documentTypeID
        order.documentCategoryID = fields.data.documentCategoryID
        order.numPages = fields.numPages.value.toInt()
        order.numSources = fields.numSources.value.toInt()
        alUAOrderRepo.save(order)

        shitToFront("9b4f1a3e-c2ca-4bfb-a567-4a612caa7fc9") {
            it.historyPushState = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))
            it.hasErrors = false
        }
        shitBigReplacementToFront("917b0edd-df3c-499d-9ffe-93f4152bddfb")
        spitOrderParamsPage(order, fields)
    }
}

object AlBackDebug {
    val idToRequestContext = ConcurrentHashMap<String, AlRequestContext>()
}

























