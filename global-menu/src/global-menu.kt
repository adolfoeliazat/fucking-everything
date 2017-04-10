package vgrechka.globalmenu

import com.sun.jna.platform.win32.User32
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.concurrent.Task
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import org.jnativehook.GlobalScreen
import org.jnativehook.mouse.NativeMouseAdapter
import org.jnativehook.mouse.NativeMouseEvent
import vgrechka.*
import vgrechka.idea.hripos.*
import vgrechka.ideabackdoor.*
import java.awt.Robot
import java.util.concurrent.ExecutorService
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.properties.Delegates.notNull
import kotlin.system.exitProcess
import java.lang.reflect.Array.setShort
import java.lang.reflect.AccessibleObject.setAccessible
import org.jnativehook.NativeInputEvent
import org.jnativehook.keyboard.NativeKeyAdapter
import org.jnativehook.keyboard.NativeKeyEvent
import java.awt.MouseInfo
import java.util.concurrent.TimeUnit
import java.util.ArrayList
import java.util.concurrent.AbstractExecutorService
import kotlin.concurrent.thread


object JFXStuff {
    fun infoAlert(headerText: String) {
        val alert = Alert(AlertType.INFORMATION)
        alert.title = "Fucking Information"
        alert.headerText = headerText
        alert.showAndWait()
    }

    fun errorAlert(e: Throwable) {
        errorAlert(e.message ?: "Obscure exception", e.stackTraceString, width = 1000)
    }

    fun errorAlert(headerText: String, contentText: String? = null, width: Int? = null) {
        val alert = Alert(AlertType.ERROR)
        alert.title = "Bloody Error"
        alert.headerText = headerText
        alert.contentText = contentText
        alert.isResizable = true
        width?.let {alert.dialogPane.prefWidth = it.toDouble()}
        alert.showAndWait()
    }
}

interface MenuItem {
    fun makeDetailsControl(): Control?
    fun run()
}

object FuckItem : MenuItem {
    override fun toString() = "Fuck"
    override fun makeDetailsControl() = Label("I am the Fuck item")

    override fun run() {
        JFXStuff.infoAlert("Fuck, fuck, fuck...")
    }
}

object ShitItem : MenuItem {
    override fun toString() = "Shit"
    override fun makeDetailsControl() = null

    override fun run() {
        JFXStuff.errorAlert("Shit? What shit?", "No shit for you today...")
    }
}

object BitchItem : MenuItem {
    override fun toString() = "Bitch"
    override fun makeDetailsControl() = Label("I am the Bitch item")

    override fun run() {
        JFXStuff.errorAlert("Fuck you. Just fuck you")
    }
}

object GlobalMenuItem_Phizdets_MakeSenseOfPHPSpew : MenuItem {
    var spew by notNull<String>()

    override fun toString() = "Phizdets: Make sense of PHP spew"

    override fun makeDetailsControl() = TextArea()-{o->
        o.minHeight = 100.0
        o.text = "Looking at clipboard..."

        thread {
            Thread.sleep(250)
            Platform.runLater {
                spew = Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT)?.toString()
                    ?: "Nothing interesting in clipboard"
                o.text = spew
            }
        }
    }

    override fun run() {
        try {
            GlobalMenuGlobal.primaryStage.isIconified = true
            sendCommandToIDEABackdoor(Command_PhiMakeSenseOfPHPSpew(spew))
            GlobalMenuGlobal.primaryStage.isIconified = true
        } catch(e: Exception) {
            JFXStuff.errorAlert(e)
        }
    }
}

object GlobalMenuGlobal {
    var primaryStage by notNull<Stage>()
}

private class VoidDispatchService : AbstractExecutorService() {
    private var running = false

    init {
        running = true
    }

    override fun shutdown() {
        running = false
    }

    override fun shutdownNow(): List<Runnable> {
        running = false
        return ArrayList(0)
    }

    override fun isShutdown(): Boolean {
        return !running
    }

    override fun isTerminated(): Boolean {
        return !running
    }

    @Throws(InterruptedException::class)
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        return true
    }

    override fun execute(r: Runnable) {
        r.run()
    }
}

class StartGlobalMenu : Application() {
    var vbox by notNull<VBox>()
    var listView by notNull<ListView<MenuItem>>()

    fun syncSelectedItem() {
        vbox.children.remove(1, vbox.children.size)
        val item = listView.selectionModel.selectedItem
        if (item != null) {
            item.makeDetailsControl()?.let {vbox.children += it}
        }
    }

    override fun start(primaryStage: Stage) {
        GlobalMenuGlobal.primaryStage = primaryStage
        val robot = Robot()

        run { // Install hook
            val logger = Logger.getLogger(GlobalScreen::class.java.`package`.name)
            logger.level = Level.WARNING
            logger.useParentHandlers = false

//            GlobalScreen.setEventDispatcher(VoidDispatchService())
            GlobalScreen.registerNativeHook()

            GlobalScreen.addNativeKeyListener(object:NativeKeyAdapter() {
                override fun nativeKeyPressed(e: NativeKeyEvent) {
                    try {
                        if (e.modifiers.and(NativeMouseEvent.CTRL_L_MASK) == NativeMouseEvent.CTRL_L_MASK) {
                            if (e.keyCode == NativeKeyEvent.VC_1) {
                                try {
//                                    robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL)
                                    robot.keyPress('C'.toInt())
                                    // clog("Copied to clipboard")
                                } finally {
                                    robot.keyRelease('C'.toInt())
//                                    robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL)
                                }

                                Platform.runLater {
                                    try {
//                                        val loc = MouseInfo.getPointerInfo().location
//                                        primaryStage.x = loc.x.toDouble()
//                                        primaryStage.y = loc.y.toDouble()
                                        primaryStage.isIconified = true
                                        primaryStage.isIconified = false
                                        syncSelectedItem()
                                    } catch(e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    } catch(e: Throwable) {
                        e.printStackTrace()
                    }
                }
            })
        }

        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN) {e->
            exitProcess(0)
        }

        vbox = VBox()
        listView = ListView<MenuItem>(FXCollections.observableArrayList(
            GlobalMenuItem_Phizdets_MakeSenseOfPHPSpew,
            FuckItem,
            ShitItem,
            BitchItem))
        vbox.children += listView

        listView.selectionModel.selectedItemProperty().addListener {observable, oldValue, newValue ->
            syncSelectedItem()
        }

        listView.selectionModel.selectFirst()
        listView.addEventHandler(KeyEvent.KEY_PRESSED) {e->
            if (e.code == KeyCode.ENTER) {
                listView.selectionModel.selectedItem.run()
            }
            else if (e.code == KeyCode.ESCAPE) {
                primaryStage.isIconified = true
            }
        }
        listView.addEventHandler(MouseEvent.MOUSE_CLICKED) {e->
            if (e.clickCount == 2) {
                listView.selectionModel.selectedItem.run()
            }
        }

        val scene = Scene(vbox, 300.0, 250.0)

        primaryStage.title = "Global Menu"
        primaryStage.scene = scene
        primaryStage.initStyle(StageStyle.DECORATED)
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(StartGlobalMenu::class.java, *args)
        }
    }
}


