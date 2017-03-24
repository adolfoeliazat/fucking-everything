package fekjs.pizdatron

import fekjs.*
import fekjs.node.*
import org.w3c.dom.events.Event

@JsName("(require('electron').app)")
external object app {
    fun on(event: String, block: Function<*>)
    fun quit()
}

fun app.onReady(block: () -> Unit) = on("ready", block)
fun app.onActivate(block: () -> Unit) = on("activate", block)
fun app.onWindowAllClosed(block: () -> Unit) = on("window-all-closed", block)

@JsName("(require('electron').BrowserWindow)")
external class BrowserWindow(opts: BrowserWindowOpts? = null) {
    val webContents: WebContents
    fun loadURL(url: String)
    fun maximize()
    fun on(eventName: String, block: Function<*>)
}

fun BrowserWindow.onClosed(block: () -> Unit) = on("closed", block)
fun BrowserWindow.onPageTitleUpdated(block: (e: Event, title: String) -> Unit) = on("page-title-updated", block)

external interface WebContents {
    fun openDevTools()
    fun send(channel: String, payload: Any?)
}

class BrowserWindowOpts(
    val width: Int = 800,
    val height: Int = 600,
    val webPreferences: WebPreferences? = null
)

class WebPreferences(
    val zoomFactor: Double = 1.0
)


@JsName("(require('electron').ipcMain)")
external object ipcMain {
    fun on(channel: String, block: (event: IPCMainEvent, payload: Any?) -> Unit)
}

external interface IPCMainEvent {
    val sender: WebContents
    var returnValue: Any?
}

@JsName("(require('electron').ipcRenderer)")
external object ipcRenderer {
    fun on(channel: String, block: (event: IPCRenderEvent, payload: Any?) -> Unit)
    fun sendSync(channel: String, payload: Any?)
    fun send(channel: String, payload: Any?)
}

external interface IPCRenderEvent

class PizdatronRenderProcess {
    init {
        console.log("got sync", ipcRenderer.sendSync("synchronous-message", "ping"))

        ipcRenderer.on("asynchronous-reply") {event: IPCRenderEvent, payload: Any? ->
            console.log("got async", payload)
        }
        ipcRenderer.send("asynchronous-message", "ping")
    }
}

class PizdatronMainProcess {
    var win: BrowserWindow? = null

    init {
        app.onReady {
            createWindow()

            ipcMain.on("asynchronous-message") {event: IPCMainEvent, payload: Any? ->
                console.log("got async", payload)
                event.sender.send("asynchronous-reply", "pong")
            }

            ipcMain.on("synchronous-message") {event: IPCMainEvent, payload: Any? ->
                console.log("got sync", payload)
                event.returnValue = "pong"
            }
        }

        app.onWindowAllClosed {
            // On macOS it is common for applications and their menu bar
            // to stay active until the user quits explicitly with Cmd + Q
            if (process.platform != "darwin") {
                app.quit()
            }
        }

        app.onActivate {
            // On macOS it's common to re-create a window in the app when the
            // dock icon is clicked and there are no other windows open.
            if (win == null) {
                createWindow()
            }
        }
    }

    fun createWindow() {
        win = BrowserWindow(BrowserWindowOpts(webPreferences = WebPreferences(zoomFactor = 1.25)))-{o->
            o.maximize()

            o.loadURL(url.format(URLObject(
                pathname = path.join(__dirname, "fekjs/pizdatron/pizdaindex.html"),
                protocol = "file:",
                slashes = true
            )))

            o.webContents.openDevTools()

            o.onClosed {
                win = null
            }
        }
    }
}




















