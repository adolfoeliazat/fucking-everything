package vgrechka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.builder.api.DefaultApi20
import javafx.application.Application
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import vgrechka.HTTPPile.StringResponse
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.properties.Delegates.notNull

// https://apps.dev.microsoft.com
// https://developer.microsoft.com/en-us/graph/graph-explorer
// https://developer.microsoft.com/en-us/graph/docs/concepts/auth_overview
// https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/resources/onedrive

class OneDrive {
//    val redirectURIPort = 8000
    val uploadChunkSize = 320 * 1024 * 32 // OneDrive wants chunk size to be divisible by 320KB

    var state: AuthenticationState = AuthenticationState.INITIAL
    var accessToken by notNull<String>()
    var code by notNull<String>()
    val once = Once()

    var account by notNullOnce<Account>()
    class Account(val displayName: String)

    val debug_actLikeGotAccessToken = false

    enum class AuthenticationState {
        INITIAL, VIRGIN, HAS_CODE, HAS_ACCESS_TOKEN, ACCESS_TOKEN_SEEMS_VALID
    }

    fun getDirToStoreAccessTokenIn() =
        FilePile.ensuringDirectoryExists("c:/tmp/${this::class.qualifiedName}-shit")

    init {
        val secrets = BigPile.saucerfulOfSecrets.onedrive.pepezdus
        clog("secrets.scopes = " + secrets.scopes)
        val service = ServiceBuilder()
            .apiKey(secrets.tokenID)
//            .callback("http://localhost:$redirectURIPort")
            .callback(secrets.redirectURI)
            .scope(secrets.scopes)
            .build(object : DefaultApi20() {
                override fun getAuthorizationBaseUrl(): String {
                    return secrets.authorityURL + secrets.authorizationEndpoint
                }

                override fun getAccessTokenEndpoint(): String {
                    return secrets.authorityURL + secrets.tokenEndpoint
                }
            })

        val codeFile = File(getDirToStoreAccessTokenIn().path + "/code")
        val accessTokenFile = File(getDirToStoreAccessTokenIn().path + "/access-token")

        o@while (true) {
            clog("state = $state")
            exhaustive=when (state) {
                AuthenticationState.ACCESS_TOKEN_SEEMS_VALID -> {
                    break@o
                }

                AuthenticationState.INITIAL -> {
                    if (accessTokenFile.exists()) {
                        accessToken = accessTokenFile.readText()
                        state = AuthenticationState.HAS_ACCESS_TOKEN
                    } else {
                        if (codeFile.exists()) {
                            code = codeFile.readText()
                            state = AuthenticationState.HAS_CODE
                        } else {
                            state = AuthenticationState.VIRGIN
                        }
                    }
                }

                AuthenticationState.VIRGIN -> {
                    val url = service.getAuthorizationUrl(mapOf())
                    code = ObtainCodeViaJavaFX(url).ignite()
                    codeFile.writeText(code)
                    state = AuthenticationState.HAS_CODE
                }

                AuthenticationState.HAS_CODE -> {
                    try {
                        accessToken = when {
                            debug_actLikeGotAccessToken -> "fucking-token"
                            else -> {
                                val token = service.getAccessToken(code)
                                clog("token.rawResponse = " + token.rawResponse)
                                token.accessToken
                            }
                        }
                        accessTokenFile.writeText(accessToken)
                        state = AuthenticationState.HAS_ACCESS_TOKEN
                    } catch(e: Throwable) {
                        state = AuthenticationState.VIRGIN
                    }
                }

                AuthenticationState.HAS_ACCESS_TOKEN -> {
                    try {
                        val request = Request.Builder()
                            .url("https://graph.microsoft.com/v1.0/me/")
                            .header("Authorization", "Bearer " + accessTokenFile.readText())
                            .header("Content-type", "application/json")
                            .get()
                            .build()

                        val response = HTTPPile.send_receiveUTF8(request)
                        // clog(response.body)
                        if (response.code != HTTPPile.code.ok) {
                            state = AuthenticationState.VIRGIN
                        } else {
                            val map = ObjectMapper().readValue(response.body, Map::class.java)
                            account = Account(displayName = map["displayName"] as String)
                            clog("User: " + account.displayName)
                            state = AuthenticationState.ACCESS_TOKEN_SEEMS_VALID
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        state = AuthenticationState.VIRGIN
                    }
                }
            }
        }
    }

    companion object {
        val javafxLaunchedEvent = ArrayBlockingQueue<Any>(1)
    }

    class App : Application() {
        override fun start(ignore_primaryStage: Stage) {
            Platform.setImplicitExit(false)
            JFXPile.installUncaughtExceptionHandler_errorAlert()
            javafxLaunchedEvent.add(Any())
        }
    }

    inner class ObtainCodeViaJavaFX(val url: String) {
        val codeObtainedEvent = ArrayBlockingQueue<String>(1)

        fun ignite(): String {
            javafxLaunchedEvent.clear()
            thread {
                try {
                    Application.launch(App::class.java)
                } catch (e: IllegalStateException) {
                    if (e.message == "Application launch must not be called more than once") {
                        // OK
                        javafxLaunchedEvent.add(Any())
                    } else {
                        throw e
                    }
                }
            }
            javafxLaunchedEvent.take()

            JFXPile.later {ShowAuthenticationWindow()}
            return codeObtainedEvent.take()
        }

        inner class ShowAuthenticationWindow {
            val stage = Stage()
            val stackPane = StackPane()
            val vbox = VBox()
            val loadWorker: Worker<Void>
            val progressIndicator = ProgressIndicator()
            val engine: WebEngine
            val once = Once()

            init {
                Thread.setDefaultUncaughtExceptionHandler {thread, exception ->
                    showException(exception)
                }

                stage.width = 500.0
                stage.height = 600.0
                stage.title = "Authentication"

                vbox.children += MenuBar()-{o->
                    o.menus += Menu("_Tools")-{o->
                        o.addItem("_Mess Around") {
                            yieldCode("pizda")
                        }
                    }
                }

                val webView = WebView()
                vbox.children += webView
                engine = webView.engine
                loadWorker = engine.loadWorker

//                debug_doSomeShit()

                loadWorker.progressProperty().addListener {_,_,_-> handleLoadWorkerState()}
                loadWorker.stateProperty().addListener {_,_,_-> handleLoadWorkerState()}
                engine.load(url)
                stackPane.children += vbox

                stackPane.children += progressIndicator

                if (!once.wasExecuted(this::showIllegalStateScene)) {
                    stage.scene = Scene(stackPane)
                }
                stage.show()
            }

            fun showIllegalStateScene(text: String) {
                once(this::showIllegalStateScene) {
                    val label = Label(text)
                    StackPane.setAlignment(label, Pos.TOP_LEFT)
                    label.alignment = Pos.TOP_LEFT
                    stage.scene = Scene(label)
                }
            }

            fun showException(e: Throwable) {
                e.printStackTrace()
                showIllegalStateScene(e.stackTraceString)
            }

            fun yieldCode(code: String) {
                once(this::yieldCode) {
                    codeObtainedEvent.add(code)
                    stage.close()
                }
            }

            fun handleLoadWorkerState() {
                // clog("progress = " + loadWorker.progress)
                // clog("state = " + loadWorker.state)
                // clog("location = " + engine.location)
                if (loadWorker.state == Worker.State.FAILED) {
                    showException(loadWorker.exception)
                    return
                }

                progressIndicator.isVisible = loadWorker.state in setOf(Worker.State.SCHEDULED, Worker.State.RUNNING)

                val loc = engine.location
                val prefix = "https://login.microsoftonline.com/common/oauth2/nativeclient?code="
                if (loc.startsWith(prefix))
                    yieldCode(loc.substring(prefix.length))
            }

            fun debug_doSomeShit() {
                if (false) {
                    thread {
                        while (true) {
                            Thread.sleep(3000)
                            JFXPile.later {showException(Exception("Всему пиздец ${Date()}"))}
                        }
                    }
                }
                if (false) {
                    thread {
                        Thread.sleep(3000)
                        JFXPile.later {yieldCode("pipiska")}
                    }
                }
            }
        }

    }

//    private fun obtainCodeViaLocalWebServer(url: String): String {
//        var code by notNullOnce<String>()
//
//        val server = Server(redirectURIPort)
//        server.handler = ServletHandler() - {o ->
//            o.addServletWithMapping(ServletHolder(object : HttpServlet() {
//                override fun service(req: HttpServletRequest, res: HttpServletResponse) {
//                    code = req.getParameter("code")
//                    res.contentType = "text/html; charset=utf-8"
//                    res.writer.println("Fuck you")
//                    res.status = HttpServletResponse.SC_OK
//                    res.flushBuffer()
//                    thread {
//                        // If not in separate thread, Jetty hangs
//                        server.stop()
//                    }
//                }
//            }), "/*")
//        }
//        server.start()
//        clog("Shit is spinning")
//        clog("Move your ass here: " + url)
//
//        server.join()
//        return code
//    }

    fun createFolder(remotePath: String) {
        val lastSlash = remotePath.lastIndexOfOrNull("/") ?: bitch("0981f6c9-4a97-4b3e-8776-d6408acf1de3")
        val name = remotePath.substring(lastSlash + 1)
        val parentPath = remotePath.substring(0, lastSlash)
        check(parentPath.startsWith("/"))
        check(!parentPath.endsWith("/"))
        check(!name.contains("/"))

        val res = post("/me/drive/root:$parentPath:/children", mapOf(
            "name" to name,
            "folder" to mapOf<Any, Any>()
        ))
        bitchUnlessCode(res, HTTPPile.code.created, "Failed to create OneDrive folder")
    }

    private fun bitchUnlessCode(res: HTTPPile.StringResponse, expectedCode: Int, message: String) {
        if (res.code != expectedCode) {
            throw Exception(message + "\n"
                                + "HTTP code: ${res.code} (expecting $expectedCode)\n\n"
                                + JSONPile.prettyPrint(res.body))
        }
    }

    private fun bitchUnlessCode(res: Response, expectedCode: Int, message: String) {
        if (res.code() != expectedCode) {
            throw Exception(message + "\n"
                                + "HTTP code: ${res.code()} (expecting $expectedCode)\n\n"
                                + JSONPile.prettyPrint(res.readUTF8()))
        }
    }

    private fun post(url: String, body: Map<String, Any>): HTTPPile.StringResponse {
        check(url.startsWith("/"))
        val request = Request.Builder()
            .url("https://graph.microsoft.com/v1.0" + url)
            .header("Authorization", "Bearer " + accessToken)
            .post(HTTPPile.makeJSONRequestBody(ObjectMapper().writeValueAsString(body)))
            .build()
        val response = HTTPPile.send_receiveUTF8(request)
        // clog(response.body)
        return response
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Ser class JSON_CreateUploadSessionResponse(
        val uploadUrl: String,
        val expirationDateTime: String,
        val nextExpectedRanges: List<String>)

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Ser class JSON_UploadChunkAcceptedResponse(
        val nextExpectedRanges: List<String>)

    fun getRangeStart(nextExpectedRanges: List<String>): Long {
        check(nextExpectedRanges.size == 1) {"35c076a4-4fee-498b-975a-3ab35f79d6bb"}
        val s = nextExpectedRanges.first()
        val mr = Regex("(\\d+)-.*").matchEntire(s)
            ?: bitch("Weird range: `$s`    2bc128c9-8e7f-4757-a233-df8b8320b232")
        return mr.groupValues[1].toLong()
    }

    fun uploadFile(file: File, remoteFilePath: String) {
        val sessionRawResponse = post("/me/drive/root:$remoteFilePath:/createUploadSession", mapOf())
        bitchUnlessCode(sessionRawResponse, HTTPPile.code.ok, "Failed to create OneDrive upload session")
        val sessionResponse = ObjectMapper().readValue(sessionRawResponse.body, JSON_CreateUploadSessionResponse::class.java)
        var rangeFrom = getRangeStart(sessionResponse.nextExpectedRanges)
        val raf = RandomAccessFile(file, "r")
        raf.use {
            loop@while (true) {
                val bytesLeft = raf.length() - rangeFrom
                val chunkSize = when {
                    bytesLeft < uploadChunkSize -> bytesLeft.toInt()
                    else -> uploadChunkSize
                }
                val chunk = ByteArray(chunkSize)

                raf.seek(rangeFrom)
                val bytesRead = raf.read(chunk)
                if (bytesRead != chunk.size) bitch("75cdb36d-998e-49bc-b014-848d9f90d110")

                val uploadRequest = Request.Builder()
                    .url(sessionResponse.uploadUrl)
                    .header("Content-Length", "${chunk.size}")
                    .header("Content-Range", "bytes $rangeFrom-${rangeFrom + chunk.size - 1}/${raf.length()}")
                    .put(RequestBody.create(null, chunk))
                    .build()

                val uploadRawResponse = HTTPPile.send_receiveUTF8(uploadRequest)
                when (uploadRawResponse.code) {
                    HTTPPile.code.accepted -> {
                        val uploadResponse = ObjectMapper().readValue(uploadRawResponse.body, JSON_UploadChunkAcceptedResponse::class.java)
                        rangeFrom = getRangeStart(uploadResponse.nextExpectedRanges)
                    }
                    HTTPPile.code.created -> {
                        break@loop
                    }
                    else -> {
                        throw Exception("Failed to upload chunk to OneDrive" + "\n"
                                            + "HTTP code: ${uploadRawResponse.code} (expecting either ${HTTPPile.code.accepted} or ${HTTPPile.code.created})\n\n"
                                            + JSONPile.prettyPrint(uploadRawResponse.body))
                    }
                }
            }
        }
    }

    fun downloadFile(remotePath: String, stm: FileOutputStream) {
        check(remotePath.startsWith("/"))
        val request = Request.Builder()
            .url("https://graph.microsoft.com/v1.0/me/drive/root:$remotePath:/content")
            .header("Authorization", "Bearer " + accessToken)
            .get()
            .build()
        val client = OkHttpClient.Builder()
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
        val response = client.newCall(request).execute()
        bitchUnlessCode(response, HTTPPile.code.ok, "Failed to download shit from OneDrive")
        val chunk = ByteArray(1024 * 1024)
        response.body().byteStream().use {istm->
            while (true) {
                val bytesRead = istm.read(chunk)
                if (bytesRead == -1)
                    break
                stm.write(chunk, 0, bytesRead)
            }
        }
    }
}

private fun Menu.addItem(title: String, handler: () -> Unit) {
    items += MenuItem(title)-{o->
        o.onAction = EventHandler {e->
            handler()
        }
    }
}










