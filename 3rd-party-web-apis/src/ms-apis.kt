package vgrechka

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
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.io.File
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.concurrent.thread
import kotlin.properties.Delegates.notNull

// https://apps.dev.microsoft.com
// https://developer.microsoft.com/en-us/graph/graph-explorer
// https://developer.microsoft.com/en-us/graph/docs/concepts/auth_overview

class OneDrive {
    val redirectURIPort = 8000
    var state: AuthenticationState = AuthenticationState.INITIAL
    var accessToken by notNull<String>()
    var code by notNull<String>()
    val once = Once()

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
                        val res = HTTPClientRequest()
                            .url("https://graph.microsoft.com/v1.0/me/")
                            .headers(listOf(
                                "Authorization" to "Bearer " + accessTokenFile.readText(),
                                "Content-type" to "application/json"))
                            .bitchUnless200(false)
                            .method_get()
                            .ignite()
                        // clog(res)

                        if (res.code == 200) {
                            val map = ObjectMapper().readValue(res.body, Map::class.java)
                            clog("User: " + map["displayName"])
                            state = AuthenticationState.ACCESS_TOKEN_SEEMS_VALID
                        } else {
                            state = AuthenticationState.VIRGIN
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

    private fun obtainCodeViaLocalWebServer(url: String): String {
        var code by notNullOnce<String>()

        val server = Server(redirectURIPort)
        server.handler = ServletHandler() - {o ->
            o.addServletWithMapping(ServletHolder(object : HttpServlet() {
                override fun service(req: HttpServletRequest, res: HttpServletResponse) {
                    code = req.getParameter("code")
                    res.contentType = "text/html; charset=utf-8"
                    res.writer.println("Fuck you")
                    res.status = HttpServletResponse.SC_OK
                    res.flushBuffer()
                    thread {
                        // If not in separate thread, Jetty hangs
                        server.stop()
                    }
                }
            }), "/*")
        }
        server.start()
        clog("Shit is spinning")
        clog("Move your ass here: " + url)

        server.join()
        return code
    }
}



private fun Menu.addItem(title: String, handler: () -> Unit) {
    items += MenuItem(title)-{o->
        o.onAction = EventHandler {e->
            handler()
        }
    }
}


